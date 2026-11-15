package com.zheiry.barbersystem.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class RailwayPortConfiguracaoTest {
    private static final String PORT_PLACEHOLDER = lerPlaceholderPorta();

    @Test
    void portaRailwayTemPrioridadeSobreServerPort() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("PORT", "9123")
                .withProperty("SERVER_PORT", "8080");

        assertThat(environment.resolveRequiredPlaceholders(PORT_PLACEHOLDER)).isEqualTo("9123");
    }

    @Test
    void serverPortMantemFallbackParaExecucaoLocal() {
        MockEnvironment environment = new MockEnvironment().withProperty("SERVER_PORT", "8081");

        assertThat(environment.resolveRequiredPlaceholders(PORT_PLACEHOLDER)).isEqualTo("8081");
    }

    @Test
    void portaPadraoContinuaDisponivelSemVariaveis() {
        assertThat(new MockEnvironment().resolveRequiredPlaceholders(PORT_PLACEHOLDER)).isEqualTo("8080");
    }

    private static String lerPlaceholderPorta() {
        try (var input = Files.newInputStream(Path.of("src/main/resources/application.properties"))) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("server.port");
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
