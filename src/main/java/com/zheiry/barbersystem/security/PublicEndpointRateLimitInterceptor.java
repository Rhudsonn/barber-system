package com.zheiry.barbersystem.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PublicEndpointRateLimitInterceptor implements HandlerInterceptor {
    private final TentativasPublicasLimitadas limites;
    private final ResolvedorIpCliente resolvedorIp;

    public PublicEndpointRateLimitInterceptor(TentativasPublicasLimitadas limites, ResolvedorIpCliente resolvedorIp) {
        this.limites = limites;
        this.resolvedorIp = resolvedorIp;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
            String contextPath = request.getContextPath();
            if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
        }
        String ip = resolvedorIp.resolver(request);
        switch (path) {
            case "/login" -> limites.verificarLoginPorIp(ip);
            case "/clientes" -> limites.verificarCadastroPorIp(ip);
            case "/login/esqueci-senha" -> limites.verificarRecuperacaoPorIp(ip);
            default -> { }
        }
        return true;
    }
}
