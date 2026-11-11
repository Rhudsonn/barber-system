package com.zheiry.barbersystem.config;

import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
class FlywaySchemaCompatibilityTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    Flyway flyway;

    @Test
    void migrationCriaSchemaCompativelComAsEntidades() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("6");
    }

    @Test
    void atualizaBancoNaV4EReconstroiHistorico() {
        String schema = "upgrade_v4";
        Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .schemas(schema).target("4").load().migrate();
        var jdbc = new JdbcTemplate(new DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
        jdbc.update("""
                INSERT INTO upgrade_v4.barbeiros (id, nome, data_nascimento, telefone, especialidade, email, senha, nivel_acesso, status)
                VALUES (1, 'Barbeiro teste', DATE '1990-01-01', '11900000000', 'Corte', 'barbeiro@example.test', 'fixture', 'BARBEIRO', 'ATIVO')
                """);
        jdbc.update("""
                INSERT INTO upgrade_v4.servicos (id, nome, descricao, categoria, preco, duracao_em_minutos, status)
                VALUES (1, 'Corte', 'Corte teste', 'CORTE', 40, 30, 'ATIVO')
                """);
        jdbc.update("""
                INSERT INTO upgrade_v4.agendamentos (id, cliente_avulso_nome, barbeiro_id, servico_id, data_hora_inicio,
                    data_hora_fim, status_agendamento, criado_em, forma_pagamento, valor_total_comanda)
                VALUES (1, 'Cliente teste', 1, 1, TIMESTAMP '2026-01-01 12:00:00',
                    TIMESTAMP '2026-01-01 12:30:00', 'CONCLUIDO', TIMESTAMP '2026-01-01 10:00:00', 'PIX', 40)
                """);
        var upgrade = Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .schemas(schema).load();
        upgrade.migrate();

        assertThat(upgrade.info().current().getVersion().getVersion()).isEqualTo("6");
        assertThat(jdbc.queryForMap("""
                SELECT tipo_evento, cliente_nome, forma_pagamento, usuario_email
                FROM upgrade_v4.historico_agendamentos WHERE agendamento_id = 1
                """))
                .containsEntry("tipo_evento", "CONCLUIDO")
                .containsEntry("cliente_nome", "Cliente teste")
                .containsEntry("forma_pagamento", "PIX")
                .containsEntry("usuario_email", null);
    }
}
