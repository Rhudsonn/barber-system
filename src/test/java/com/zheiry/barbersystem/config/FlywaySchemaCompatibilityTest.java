package com.zheiry.barbersystem.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flyway-schema-validation;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class FlywaySchemaCompatibilityTest {

    @Test
    void migrationCriaSchemaCompativelComAsEntidades() {
        // A inicialização do contexto executa Flyway e a validação do Hibernate.
    }
}
