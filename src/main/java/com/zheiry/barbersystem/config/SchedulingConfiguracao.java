package com.zheiry.barbersystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Scheduling is opt-in and is never enabled for staging. */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@Profile("!staging")
@ConditionalOnProperty(prefix = "app.scheduling", name = "enabled", havingValue = "true")
public class SchedulingConfiguracao {
}
