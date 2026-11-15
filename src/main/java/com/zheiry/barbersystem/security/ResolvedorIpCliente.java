package com.zheiry.barbersystem.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Só aceita X-Forwarded-For quando o peer TCP é confiável.
 * Configure app.security.rate-limit.trusted-proxies com IPs literais separados por vírgula;
 * o proxy de borda deve substituir ou acrescentar corretamente o endereço real do cliente.
 */
@Component
public class ResolvedorIpCliente {
    private final Set<String> proxiesConfiaveis;

    public ResolvedorIpCliente(@Value("${app.security.rate-limit.trusted-proxies:}") String configuracao) {
        this.proxiesConfiaveis = Arrays.stream(configuracao.split(","))
                .map(String::trim)
                .filter(valor -> !valor.isEmpty())
                .map(ResolvedorIpCliente::normalizarIp)
                .collect(Collectors.toUnmodifiableSet());
    }

    public String resolver(HttpServletRequest request) {
        String peer = normalizarOuDesconhecido(request.getRemoteAddr());
        if (!proxiesConfiaveis.contains(peer)) return peer;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor == null || xForwardedFor.isBlank()) return peer;
        List<String> cadeia = Arrays.stream(xForwardedFor.split(","))
                .map(String::trim).toList();
        String saltoAtual = peer;
        for (int i = cadeia.size() - 1; i >= 0; i--) {
            if (!proxiesConfiaveis.contains(saltoAtual)) return saltoAtual;
            String candidato = normalizarOuNulo(cadeia.get(i));
            if (candidato == null) return peer; // Cabeçalho malformado: ignorar a cadeia inteira.
            saltoAtual = candidato;
        }
        return saltoAtual;
    }

    private static String normalizarOuDesconhecido(String valor) {
        String ip = normalizarOuNulo(valor);
        return ip == null ? "desconhecido" : ip;
    }

    private static String normalizarIp(String valor) {
        String ip = normalizarOuNulo(valor);
        if (ip == null) throw new IllegalArgumentException("Proxy confiável deve ser um endereço IP literal.");
        return ip;
    }

    private static String normalizarOuNulo(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String candidato = valor.trim();
        // Não permitir resolução DNS de valores enviados pelo cliente.
        if (!candidato.matches("[0-9.]+") && !candidato.matches("[0-9a-fA-F:.%]+")) return null;
        try {
            InetAddress endereco = InetAddress.getByName(candidato);
            return endereco.getHostAddress();
        } catch (UnknownHostException ex) {
            return null;
        }
    }
}
