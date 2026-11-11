package com.zheiry.barbersystem.config.jwt;

import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        setField("secretKey", "a-test-secret-with-at-least-32-characters-long");
        setField("expirationMs", 60_000L);
    }

    @Test
    void tokenNaoEValidoQuandoContaEstaDesativada() {
        UsuarioAutenticado ativo = usuario(true);
        String token = jwtService.gerarToken(ativo);

        assertFalse(jwtService.tokenValido(token, usuario(false)));
    }

    @Test
    void tokenValidoParaContaAtivaEUsuarioCorrespondente() {
        UsuarioAutenticado ativo = usuario(true);
        String token = jwtService.gerarToken(ativo);

        assertTrue(jwtService.tokenValido(token, ativo));
    }

    private UsuarioAutenticado usuario(boolean ativo) {
        return new UsuarioAutenticado(12L, "cliente@example.com", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")), ativo);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = JwtService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(jwtService, value);
    }
}
