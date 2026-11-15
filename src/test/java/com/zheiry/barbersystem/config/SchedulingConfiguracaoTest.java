package com.zheiry.barbersystem.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingConfiguracaoTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SchedulingConfiguracao.class);

    @Test
    void desativaSchedulingQuandoPropriedadeEstaAusente() {
        contextRunner.run(context ->
                assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class));
    }

    @Test
    void ativaSchedulingQuandoHabilitadoExplicitamenteEmPerfilPermitido() {
        contextRunner.withPropertyValues("app.scheduling.enabled=true", "spring.profiles.active=prod")
                .run(context ->
                        assertThat(context).hasSingleBean(ScheduledAnnotationBeanPostProcessor.class));
    }

    @Test
    void mantemSchedulingDesativadoEmStagingMesmoComAtivacaoExplicita() {
        contextRunner.withPropertyValues("app.scheduling.enabled=true", "spring.profiles.active=staging")
                .run(context ->
                        assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class));
    }
}
