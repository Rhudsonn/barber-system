package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.PostgresTestBase;
import com.zheiry.barbersystem.dto.cliente.ClienteAtualizaDto;
import com.zheiry.barbersystem.dto.cliente.ClienteDto;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.repositories.ClienteRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ClienteCadastroEdicaoIntegracaoTest extends PostgresTestBase {

    @Autowired ClienteService service;
    @Autowired ClienteRepository repository;
    @Autowired EntityManager entityManager;

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cadastroSalvaClienteNoPostgresEfemero() {
        var resposta = service.cadastroCliente(new ClienteDto(
                "João de Teste", LocalDate.of(1993, 4, 12), "(11) 98888-1234",
                "joao.integracao@example.test", "Senha#2026A", "Senha#2026A"));

        entityManager.flush();
        entityManager.clear();

        ClienteEntity persistido = repository.findById(resposta.id()).orElseThrow();
        assertEquals("João de Teste", persistido.getNome());
        assertEquals("joao.integracao@example.test", persistido.getEmail());
        org.junit.jupiter.api.Assertions.assertNotEquals("Senha#2026A", persistido.getSenha());
    }

    @Test
    void edicaoPersisteDadosQueContinuamAposRecarregarDoPostgresEfemero() {
        var cadastrado = service.cadastroCliente(new ClienteDto(
                "João de Teste", LocalDate.of(1993, 4, 12), "(11) 98888-1234",
                "joao.edicao@example.test", "Senha#2026A", "Senha#2026A"));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("joao.edicao@example.test", "", java.util.List.of()));

        service.atualizarInformacoesCliente(cadastrado.id(),
                new ClienteAtualizaDto("João Atualizado", null, null, null));
        entityManager.flush();
        entityManager.clear();

        assertEquals("João Atualizado", repository.findById(cadastrado.id()).orElseThrow().getNome());
    }
}
