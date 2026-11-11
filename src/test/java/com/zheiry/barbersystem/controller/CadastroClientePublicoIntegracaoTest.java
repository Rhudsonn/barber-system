package com.zheiry.barbersystem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CadastroClientePublicoIntegracaoTest {

    @Autowired MockMvc mockMvc;

    @Test
    void cadastroPublicoAceitaPostSemAutenticacao() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Cliente de Teste",
                                  "dataNascimento": "1995-05-10",
                                  "telefone": "(11) 99999-1234",
                                  "email": "cadastro.publico@example.test",
                                  "senha": "Senha#2026A",
                                  "confirmaSenha": "Senha#2026A"
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
