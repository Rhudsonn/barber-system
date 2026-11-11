package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaRedefinirDto;
import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaSaidaDto;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.entities.RecuperacaoSenhaEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock RecuperacaoSenhaRepository recuperacaoRepository;
    @Mock AdministradorRepository administradorRepository;
    @Mock BarbeiroRepository barbeiroRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JavaMailSender mailSender;

    private RecuperacaoSenhaService service;

    @BeforeEach
    void setUp() {
        service = new RecuperacaoSenhaService(recuperacaoRepository, administradorRepository, barbeiroRepository,
                clienteRepository, passwordEncoder, mailSender,
                "http://localhost:3000/redefinir-senha", "no-reply@example.com", 15,
                Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void solicitaRecuperacaoComRespostaGenericaQuandoEmailNaoExiste() {
        when(administradorRepository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        when(barbeiroRepository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());

        RecuperacaoSenhaSaidaDto resposta = service.solicitar("ausente@example.com");

        assertTrue(resposta.mensagem().contains("Se o e-mail estiver cadastrado"));
        verify(mailSender, never()).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void tokenDeRedefinicaoSoPodeSerUsadoUmaVez() throws Exception {
        String token = "token-de-teste";
        RecuperacaoSenhaEntity recuperacao = new RecuperacaoSenhaEntity();
        recuperacao.setEmail("cliente@example.com");
        recuperacao.setUsuarioId(5L);
        recuperacao.setNivelAcesso(NivelAcesso.CLIENTE);
        recuperacao.setTokenHash(hash(token));
        recuperacao.setExpiraEm(LocalDateTime.of(2026, 10, 25, 9, 5));
        when(recuperacaoRepository.findByTokenHash(hash(token)))
                .thenReturn(Optional.of(recuperacao), Optional.empty());

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(5L);
        cliente.setStatus(Status.ATIVO);
        cliente.setSenha("senha-hash-antiga");
        when(clienteRepository.findById(5L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("NovaSenha1!", "senha-hash-antiga")).thenReturn(false);
        when(passwordEncoder.encode("NovaSenha1!")).thenReturn("senha-hash-nova");

        RecuperacaoSenhaRedefinirDto dto = new RecuperacaoSenhaRedefinirDto(token, "NovaSenha1!", "NovaSenha1!");
        service.redefinir(dto);

        assertEquals("senha-hash-nova", cliente.getSenha());
        verify(recuperacaoRepository).deleteByEmailIgnoreCase("cliente@example.com");
        assertThrows(BadRequestException.class, () -> service.redefinir(dto));
    }

    @Test
    void tokenExpiradoNoInstanteAtualEhRejeitado() throws Exception {
        String token = "token-expirado";
        RecuperacaoSenhaEntity recuperacao = new RecuperacaoSenhaEntity();
        recuperacao.setTokenHash(hash(token));
        recuperacao.setExpiraEm(LocalDateTime.of(2026, 10, 25, 9, 0));
        when(recuperacaoRepository.findByTokenHash(hash(token))).thenReturn(Optional.of(recuperacao));

        var dto = new RecuperacaoSenhaRedefinirDto(token, "NovaSenha1!", "NovaSenha1!");
        assertThrows(BadRequestException.class, () -> service.redefinir(dto));
        verifyNoInteractions(clienteRepository, administradorRepository, barbeiroRepository);
    }

    private String hash(String token) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
    }
}
