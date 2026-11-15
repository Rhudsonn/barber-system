package com.zheiry.barbersystem.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Fails before application beans (including the datasource and schedulers) are created
 * if the effective staging configuration is unsafe or points outside the staging project.
 */
@Configuration(proxyBeanMethods = false)
@Profile("staging")
public class StagingSafetyConfiguracao {
    private static final String PROJECT_REF = "tmpgqgvnwosuidpogprw";
    private static final String DATABASE_ROLE = "barber_runtime";

    @Bean
    static BeanFactoryPostProcessor validarConfiguracaoStaging(Environment environment) {
        return beanFactory -> validar(environment);
    }

    private static void validar(Environment environment) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            throw new IllegalStateException("Staging não pode iniciar com o perfil prod ativo.");
        }

        String stagingJdbcUrl = obrigatorio(environment, "STAGING_DB_URL");
        String stagingUsuario = obrigatorio(environment, "STAGING_DB_USERNAME");
        String stagingSenha = obrigatorio(environment, "STAGING_DB_PASSWORD");
        String jdbcUrl = obrigatorio(environment, "spring.datasource.url");
        String usuario = obrigatorio(environment, "spring.datasource.username");
        String senha = obrigatorio(environment, "spring.datasource.password");
        if (!Objects.equals(jdbcUrl, stagingJdbcUrl)
                || !Objects.equals(usuario, stagingUsuario)
                || !Objects.equals(senha, stagingSenha)) {
            throw new IllegalStateException("As propriedades efetivas do datasource precisam vir das variáveis STAGING_DB_*.");
        }
        String jwtSecret = obrigatorio(environment, "jwt.secret");
        if (jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("A chave JWT de staging deve conter pelo menos 32 bytes.");
        }
        validarDestino(jdbcUrl, usuario);

        exigirValor(environment, "spring.datasource.driver-class-name", "org.postgresql.Driver");
        exigirAusente(environment, "spring.datasource.type");
        exigirAusente(environment, "spring.datasource.jndi-name");
        exigirAusente(environment, "spring.datasource.hikari.jdbc-url");
        exigirAusente(environment, "spring.datasource.hikari.data-source-class-name");
        exigirAusente(environment, "spring.datasource.hikari.driver-class-name");
        exigirAusente(environment, "spring.datasource.hikari.username");
        exigirAusente(environment, "spring.datasource.hikari.password");
        exigirAusente(environment, "spring.datasource.hikari.connection-init-sql");
        exigirSemPropriedadesDeConexaoHikari(environment);

        exigirFalso(environment, "spring.flyway.enabled");
        exigirValor(environment, "spring.jpa.hibernate.ddl-auto", "validate");
        exigirFalso(environment, "spring.jpa.generate-ddl");
        exigirValor(environment, "spring.sql.init.mode", "never");
        String hbm2ddl = environment.getProperty("spring.jpa.properties.hibernate.hbm2ddl.auto");
        if (hbm2ddl != null && !hbm2ddl.equalsIgnoreCase("validate")) {
            throw new IllegalStateException("Staging não permite DDL do Hibernate diferente de validate.");
        }
        exigirFalso(environment, "app.bootstrap-admin.enabled");
        exigirFalso(environment, "app.notificacoes.resend.ativo");
        exigirFalso(environment, "app.scheduling.enabled");
    }

    private static String obrigatorio(Environment environment, String key) {
        String value;
        try {
            value = environment.getProperty(key);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Configuração obrigatória ausente ou inválida para staging: " + key + ".");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configuração obrigatória ausente ou inválida para staging: " + key + ".");
        }
        return value;
    }

    private static void validarDestino(String jdbcUrl, String usuario) {
        try {
            if (!jdbcUrl.startsWith("jdbc:")) throw new IllegalArgumentException();
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            if (!"postgresql".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getRawUserInfo() != null) {
                throw new IllegalArgumentException();
            }

            String host = uri.getHost().toLowerCase(Locale.ROOT);
            String expectedDirectHost = "db." + PROJECT_REF + ".supabase.co";
            boolean directStaging = host.equals(expectedDirectHost)
                    && uri.getPort() == 5432
                    && usuario.equals(DATABASE_ROLE);
            boolean sharedPoolerStaging = host.endsWith(".pooler.supabase.com")
                    && (uri.getPort() == 5432 || uri.getPort() == 6543)
                    && usuario.equals(DATABASE_ROLE + "." + PROJECT_REF);
            if (!directStaging && !sharedPoolerStaging) throw new IllegalArgumentException();

            String query = uri.getRawQuery();
            boolean sslRequired = false;
            if (query != null) {
                for (String parameter : query.split("&")) {
                    String[] pair = parameter.split("=", 2);
                    if (!pair[0].equalsIgnoreCase("sslmode") || pair.length != 2
                            || !pair[1].equalsIgnoreCase("require")) throw new IllegalArgumentException();
                    sslRequired = true;
                }
            }
            if (!sslRequired) throw new IllegalArgumentException();
        } catch (RuntimeException ex) {
            // Do not include the URL or username in exceptions/logs; configuration may contain secrets.
            throw new IllegalStateException("A conexão configurada para staging não corresponde ao projeto Supabase de staging autorizado.");
        }
    }

    private static void exigirFalso(Environment environment, String key) {
        exigirValor(environment, key, "false");
    }

    private static void exigirValor(Environment environment, String key, String expected) {
        String actual = environment.getProperty(key);
        if (actual == null || !actual.equalsIgnoreCase(expected)) {
            throw new IllegalStateException("Configuração incompatível com o modo seguro de staging: " + key + ".");
        }
    }

    private static void exigirAusente(Environment environment, String key) {
        if (environment.getProperty(key) != null) {
            throw new IllegalStateException("Staging não permite override de datasource alternativo: " + key + ".");
        }
    }

    private static void exigirSemPropriedadesDeConexaoHikari(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment configurable)) {
            throw new IllegalStateException("Não foi possível validar a configuração isolada de staging.");
        }
        for (PropertySource<?> source : configurable.getPropertySources()) {
            if (source instanceof EnumerablePropertySource<?> enumerable) {
                for (String name : enumerable.getPropertyNames()) {
                    String normalized = name.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
                    if (normalized.startsWith("springdatasourcehikaridatasourceproperties")) {
                        throw new IllegalStateException("Staging não permite propriedades Hikari que possam sobrescrever a conexão validada.");
                    }
                }
            }
        }
    }

}
