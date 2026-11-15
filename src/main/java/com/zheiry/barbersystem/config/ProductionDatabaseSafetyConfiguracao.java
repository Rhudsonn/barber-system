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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Prevents production from starting with an administrative or unencrypted datasource. */
@Configuration(proxyBeanMethods = false)
@Profile("prod")
public class ProductionDatabaseSafetyConfiguracao {
    private static final String ROLE = "barber_runtime";

    @Bean
    static BeanFactoryPostProcessor validarDatasourceProducao(Environment environment) {
        return beanFactory -> validar(environment);
    }

    private static void validar(Environment environment) {
        String projectRef = required(environment, "app.supabase.project-ref");
        if (!projectRef.matches("[a-z0-9]{20}")) throw invalid("app.supabase.project-ref");

        String configuredUrl = required(environment, "DB_URL");
        String effectiveUrl = required(environment, "spring.datasource.url");
        String configuredUser = required(environment, "DB_USERNAME");
        String effectiveUser = required(environment, "spring.datasource.username");
        if (!configuredUrl.equals(effectiveUrl) || !configuredUser.equals(effectiveUser)) {
            throw invalid("spring.datasource.url/spring.datasource.username");
        }
        if (!"org.postgresql.Driver".equals(required(environment, "spring.datasource.driver-class-name"))) {
            throw invalid("spring.datasource.driver-class-name");
        }
        if (!"false".equalsIgnoreCase(environment.getProperty("spring.flyway.enabled", "false"))) {
            throw invalid("spring.flyway.enabled");
        }
        if (!"validate".equalsIgnoreCase(environment.getProperty("spring.jpa.hibernate.ddl-auto", ""))) {
            throw invalid("spring.jpa.hibernate.ddl-auto");
        }
        validarHikari(environment);
        validarDestino(effectiveUrl, effectiveUser, projectRef);
    }

    private static void validarHikari(Environment environment) {
        for (String key : new String[]{
                "spring.datasource.type", "spring.datasource.jndi-name",
                "spring.datasource.hikari.jdbc-url", "spring.datasource.hikari.data-source-class-name",
                "spring.datasource.hikari.driver-class-name", "spring.datasource.hikari.username",
                "spring.datasource.hikari.password", "spring.datasource.hikari.connection-init-sql"}) {
            if (environment.getProperty(key) != null) throw invalid(key);
        }
        if (!(environment instanceof ConfigurableEnvironment configurable)) throw invalid("Hikari");
        for (PropertySource<?> source : configurable.getPropertySources()) {
            if (!(source instanceof EnumerablePropertySource<?> enumerable)) continue;
            for (String name : enumerable.getPropertyNames()) {
                String normalized = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
                if (normalized.startsWith("springdatasourcehikaridatasourceproperties")) {
                    throw invalid("spring.datasource.hikari.data-source-properties");
                }
            }
        }
    }

    private static void validarDestino(String jdbcUrl, String username, String expectedRef) {
        try {
            if (!jdbcUrl.startsWith("jdbc:")) throw new IllegalArgumentException();
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            if (!"postgresql".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null) throw new IllegalArgumentException();

            String host = uri.getHost().toLowerCase(Locale.ROOT);
            boolean direct = host.equals("db." + expectedRef + ".supabase.co")
                    && uri.getPort() == 5432 && username.equals(ROLE);
            boolean sharedPooler = host.endsWith(".pooler.supabase.com")
                    && (uri.getPort() == 5432 || uri.getPort() == 6543)
                    && username.equals(ROLE + "." + expectedRef);
            if (!direct && !sharedPooler) throw new IllegalArgumentException();

            Map<String, String> query = parseQuery(uri.getRawQuery());
            if (!"require".equalsIgnoreCase(query.get("sslmode"))) throw new IllegalArgumentException();
            if (sharedPooler && uri.getPort() == 6543
                    && !"0".equals(query.get("preparethreshold"))) throw new IllegalArgumentException();
        } catch (RuntimeException ex) {
            throw invalid("spring.datasource.url/spring.datasource.username");
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> values = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return values;
        Arrays.stream(rawQuery.split("&")).forEach(parameter -> {
            String[] pair = parameter.split("=", 2);
            if (pair.length != 2) throw new IllegalArgumentException();
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
            String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            if (values.putIfAbsent(key, value) != null) throw new IllegalArgumentException();
        });
        return values;
    }

    private static String required(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) throw invalid(key);
        return value;
    }

    private static IllegalStateException invalid(String key) {
        return new IllegalStateException("Configuração obrigatória ausente ou inválida para produção: " + key + ".");
    }
}
