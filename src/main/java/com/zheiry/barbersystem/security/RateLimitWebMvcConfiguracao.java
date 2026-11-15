package com.zheiry.barbersystem.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitWebMvcConfiguracao implements WebMvcConfigurer {
    private final PublicEndpointRateLimitInterceptor interceptor;

    public RateLimitWebMvcConfiguracao(PublicEndpointRateLimitInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/login", "/login/esqueci-senha", "/clientes");
    }
}
