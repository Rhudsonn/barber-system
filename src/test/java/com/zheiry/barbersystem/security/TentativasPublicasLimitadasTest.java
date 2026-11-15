package com.zheiry.barbersystem.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TentativasPublicasLimitadasTest {

    @Test
    void limiteDeLoginPorContaExpiraEIgnoraDiferencasDeMaiusculas() {
        RelogioMutavel relogio = new RelogioMutavel();
        TentativasPublicasLimitadas limites = new TentativasPublicasLimitadas(relogio);

        for (int i = 0; i < 20; i++) limites.verificarLoginPorConta(" Pessoa@Exemplo.com ");
        LimiteTentativasExcedidoException bloqueio = assertThrows(LimiteTentativasExcedidoException.class,
                () -> limites.verificarLoginPorConta("pessoa@exemplo.com"));
        assertTrue(bloqueio.retryAfterSegundos() > 0);

        relogio.avancar(Duration.ofMinutes(15));
        limites.verificarLoginPorConta("pessoa@exemplo.com");
    }

    @Test
    void limitesPorIpSeparamCadastroLoginERecuperacao() {
        TentativasPublicasLimitadas limites = new TentativasPublicasLimitadas(Clock.systemUTC());
        for (int i = 0; i < 10; i++) limites.verificarCadastroPorIp("198.51.100.10");

        assertThrows(LimiteTentativasExcedidoException.class,
                () -> limites.verificarCadastroPorIp("198.51.100.10"));
        // Outro fluxo e outro IP mantêm cotas independentes.
        limites.verificarLoginPorIp("198.51.100.10");
        limites.verificarCadastroPorIp("198.51.100.11");
    }

    @Test
    void limiteDeRecuperacaoPorContaETemporario() {
        RelogioMutavel relogio = new RelogioMutavel();
        TentativasPublicasLimitadas limites = new TentativasPublicasLimitadas(relogio);
        for (int i = 0; i < 3; i++) limites.verificarRecuperacaoPorConta("conta@example.com");
        assertThrows(LimiteTentativasExcedidoException.class,
                () -> limites.verificarRecuperacaoPorConta("conta@example.com"));
        relogio.avancar(Duration.ofHours(1));
        limites.verificarRecuperacaoPorConta("conta@example.com");
    }

    @Test
    void ignoraXForwardedForQuandoPeerNaoEstaNaListaDeProxiesConfiaveis() {
        ResolvedorIpCliente resolvedor = new ResolvedorIpCliente("");
        MockHttpServletRequest request = request("198.51.100.10");
        request.addHeader("X-Forwarded-For", "203.0.113.99");

        assertEquals("198.51.100.10", resolvedor.resolver(request));
    }

    @Test
    void percorreSomenteSaltosDeProxyExplicitamenteConfiaveis() {
        ResolvedorIpCliente resolvedor = new ResolvedorIpCliente("10.0.0.1,10.0.0.2");
        MockHttpServletRequest request = request("10.0.0.1");
        request.addHeader("X-Forwarded-For", "198.51.100.25, 10.0.0.2");

        assertEquals("198.51.100.25", resolvedor.resolver(request));
    }

    @Test
    void retorna429EHeaderRetryAfterNoEndpointQuandoLimiteDeIpEExcedido() throws Exception {
        RelogioMutavel relogio = new RelogioMutavel();
        TentativasPublicasLimitadas limites = new TentativasPublicasLimitadas(relogio);
        PublicEndpointRateLimitInterceptor interceptor = new PublicEndpointRateLimitInterceptor(
                limites, new ResolvedorIpCliente(""));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CadastroStub())
                .addInterceptors(interceptor)
                .setControllerAdvice(new com.zheiry.barbersystem.exception.GlobalExceptionHandler())
                .build();

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/clientes")).andExpect(status().isCreated());
        }
        mockMvc.perform(post("/clientes"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    private static MockHttpServletRequest request(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        return request;
    }

    private static final class RelogioMutavel extends Clock {
        private final AtomicLong millis = new AtomicLong(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli());

        void avancar(Duration duracao) { millis.addAndGet(duracao.toMillis()); }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis.get()); }
    }

    @RestController
    private static class CadastroStub {
        @PostMapping("/clientes")
        ResponseEntity<Void> cadastrar() { return ResponseEntity.status(201).build(); }
    }
}
