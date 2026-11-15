package com.zheiry.barbersystem.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionDatabaseSafetyConfiguracaoTest {
    private static final String REF = "abcdefghijklmnopqrst";
    private static final String DIRECT_URL = "jdbc:postgresql://db." + REF
            + ".supabase.co:5432/postgres?sslmode=require";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(context -> context.getEnvironment().setActiveProfiles("prod"))
            .withUserConfiguration(ProductionDatabaseSafetyConfiguracao.class)
            .withPropertyValues(
                    "app.supabase.project-ref=" + REF,
                    "DB_URL=" + DIRECT_URL,
                    "spring.datasource.url=" + DIRECT_URL,
                    "DB_USERNAME=barber_runtime",
                    "spring.datasource.username=barber_runtime",
                    "spring.datasource.driver-class-name=org.postgresql.Driver",
                    "spring.flyway.enabled=false",
                    "spring.jpa.hibernate.ddl-auto=validate");

    @Test
    void aceitaRoleRuntimeComSslEProjetoCorrespondente() {
        contextRunner.run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void aceitaPoolerDeSessaoComUsernameQualificadoPeloProjeto() {
        String url = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require";
        contextRunner.withPropertyValues(
                        "DB_URL=" + url,
                        "spring.datasource.url=" + url,
                        "DB_USERNAME=barber_runtime." + REF,
                        "spring.datasource.username=barber_runtime." + REF)
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void exigePreparedStatementsDesativadosNoPoolerDeTransacao() {
        String url = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require";
        contextRunner.withPropertyValues(
                        "DB_URL=" + url,
                        "spring.datasource.url=" + url,
                        "DB_USERNAME=barber_runtime." + REF,
                        "spring.datasource.username=barber_runtime." + REF)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void aceitaPoolerDeTransacaoComPreparedStatementsDesativados() {
        String url = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
        contextRunner.withPropertyValues(
                        "DB_URL=" + url,
                        "spring.datasource.url=" + url,
                        "DB_USERNAME=barber_runtime." + REF,
                        "spring.datasource.username=barber_runtime." + REF)
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void rejeitaRoleAdministrativa() {
        contextRunner.withPropertyValues("DB_USERNAME=postgres", "spring.datasource.username=postgres")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejeitaUrlSemSslObrigatorio() {
        String url = DIRECT_URL.replace("?sslmode=require", "");
        contextRunner.withPropertyValues("DB_URL=" + url, "spring.datasource.url=" + url)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejeitaProjetoDiferenteDoIdentificadorConfigurado() {
        String url = "jdbc:postgresql://db.zyxwvutsrqponmlkjihgfedc.supabase.co:5432/postgres?sslmode=require";
        contextRunner.withPropertyValues("DB_URL=" + url, "spring.datasource.url=" + url)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejeitaOverrideDeUsuarioViaHikari() {
        contextRunner.withPropertyValues("spring.datasource.hikari.username=postgres")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejeitaFlywayHabilitadoEmRuntimeDeProducao() {
        contextRunner.withPropertyValues("spring.flyway.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void naoAplicaGuardDeProducaoAoPerfilStaging() {
        new ApplicationContextRunner()
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("staging"))
                .withUserConfiguration(ProductionDatabaseSafetyConfiguracao.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(ProductionDatabaseSafetyConfiguracao.class);
                });
    }
}
