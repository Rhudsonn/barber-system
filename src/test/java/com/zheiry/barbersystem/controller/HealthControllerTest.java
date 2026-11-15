package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.config.jwt.JwtService;
import com.zheiry.barbersystem.config.springSecurity.CustomUserDetailsService;
import com.zheiry.barbersystem.config.springSecurity.SecurityConfiguracao;
import com.zheiry.barbersystem.security.TentativasPublicasLimitadas;
import com.zheiry.barbersystem.security.ResolvedorIpCliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import(SecurityConfiguracao.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private TentativasPublicasLimitadas tentativasPublicasLimitadas;

    @MockitoBean
    private ResolvedorIpCliente resolvedorIpCliente;

    @Test
    void getHealthEPublicoERespondeSemDependenciaDeBanco() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void apenasGetHealthEPublico() throws Exception {
        mockMvc.perform(post("/health")).andExpect(status().isForbidden());
        mockMvc.perform(get("/health/privado")).andExpect(status().isForbidden());
    }
}
