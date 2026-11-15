package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaRedefinirDto;
import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaSaidaDto;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.entities.RecuperacaoSenhaEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.repositories.*;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock RecuperacaoSenhaRepository recuperacaoRepository;
    @Mock AdministradorRepository administradorRepository;
    @Mock BarbeiroRepository barbeiroRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ResendEmailService resendEmailService;

    private RecuperacaoSenhaService service;

    @BeforeEach
    void setUp() {
        service = new RecuperacaoSenhaService(recuperacaoRepository, administradorRepository, barbeiroRepository,
                clienteRepository, passwordEncoder, resendEmailService,
                "https://web.example.test/redefinir-senha", 15,
                Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void solicitaRecuperacaoComRespostaGenericaQuandoEmailNaoExiste() {
        when(administradorRepository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        when(barbeiroRepository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());

        RecuperacaoSenhaSaidaDto resposta = service.solicitar("ausente@example.com");

        assertTrue(resposta.mensagem().contains("Se o e-mail estiver cadastrado"));
        verify(resendEmailService, never()).enviarEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void solicitacoesRepetidasPreservamLinksAnterioresAteExpirarem() {
        ClienteEntity cliente = clienteAtivo();
        when(administradorRepository.findByEmailIgnoreCase("cliente@example.com")).thenReturn(Optional.empty());
        when(barbeiroRepository.findByEmailIgnoreCase("cliente@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmailIgnoreCase("cliente@example.com")).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(cliente));

        var primeiraResposta = service.solicitar("cliente@example.com");
        var segundaResposta = service.solicitar("cliente@example.com");

        assertEquals(primeiraResposta.mensagem(), segundaResposta.mensagem());
        ArgumentCaptor<RecuperacaoSenhaEntity> tokens = ArgumentCaptor.forClass(RecuperacaoSenhaEntity.class);
        verify(recuperacaoRepository, times(2)).save(tokens.capture());
        assertNotEquals(tokens.getAllValues().get(0).getTokenHash(), tokens.getAllValues().get(1).getTokenHash());
        assertEquals(LocalDateTime.of(2026, 10, 25, 9, 15), tokens.getAllValues().get(0).getExpiraEm());
        verify(recuperacaoRepository, never()).deleteByNivelAcessoAndUsuarioId(any(), any());
        verify(resendEmailService, times(2)).enviarEmail(
                eq("cliente@example.com"), eq("Redefinição de senha"), contains("https://web.example.test/redefinir-senha"),
                startsWith("recuperacao-senha:"));
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
        when(recuperacaoRepository.findByTokenHash(hash(token))).thenReturn(Optional.of(recuperacao), Optional.empty());
        when(recuperacaoRepository.findByTokenHashForUpdate(hash(token))).thenReturn(Optional.of(recuperacao));

        ClienteEntity cliente = clienteAtivo();
        cliente.setSenha("senha-hash-antiga");
        when(clienteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findById(5L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("NovaSenha1!", "senha-hash-antiga")).thenReturn(false);
        when(passwordEncoder.encode("NovaSenha1!")).thenReturn("senha-hash-nova");

        RecuperacaoSenhaRedefinirDto dto = new RecuperacaoSenhaRedefinirDto(token, "NovaSenha1!", "NovaSenha1!");
        service.redefinir(dto);

        assertEquals("senha-hash-nova", cliente.getSenha());
        verify(recuperacaoRepository).deleteByNivelAcessoAndUsuarioId(NivelAcesso.CLIENTE, 5L);
        InOrder ordem = inOrder(recuperacaoRepository, clienteRepository);
        ordem.verify(recuperacaoRepository).findByTokenHash(hash(token));
        ordem.verify(clienteRepository).findByIdForUpdate(5L);
        ordem.verify(recuperacaoRepository).findByTokenHashForUpdate(hash(token));
        assertThrows(BadRequestException.class, () -> service.redefinir(dto));
    }

    @Test
    void tokenExpiradoNoInstanteAtualEhRejeitado() throws Exception {
        String token = "token-expirado";
        RecuperacaoSenhaEntity recuperacao = new RecuperacaoSenhaEntity();
        recuperacao.setTokenHash(hash(token));
        recuperacao.setEmail("cliente@example.com");
        recuperacao.setUsuarioId(5L);
        recuperacao.setNivelAcesso(NivelAcesso.CLIENTE);
        recuperacao.setExpiraEm(LocalDateTime.of(2026, 10, 25, 9, 0));
        when(recuperacaoRepository.findByTokenHash(hash(token))).thenReturn(Optional.of(recuperacao));
        when(clienteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(clienteAtivo()));
        when(recuperacaoRepository.findByTokenHashForUpdate(hash(token))).thenReturn(Optional.of(recuperacao));

        var dto = new RecuperacaoSenhaRedefinirDto(token, "NovaSenha1!", "NovaSenha1!");
        assertThrows(BadRequestException.class, () -> service.redefinir(dto));
        verify(clienteRepository).findByIdForUpdate(5L);
        verify(clienteRepository, never()).findById(5L);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void redefinicaoRejeitaTokenConsumidoAntesDaReleituraBloqueada() throws Exception {
        String token = "token-ja-consumido";
        RecuperacaoSenhaEntity recuperacao = recuperacao(token, LocalDateTime.of(2026, 10, 25, 9, 5));
        when(recuperacaoRepository.findByTokenHash(hash(token))).thenReturn(Optional.of(recuperacao));
        when(clienteRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(clienteAtivo()));
        when(recuperacaoRepository.findByTokenHashForUpdate(hash(token))).thenReturn(Optional.empty());

        var dto = new RecuperacaoSenhaRedefinirDto(token, "NovaSenha1!", "NovaSenha1!");
        assertThrows(BadRequestException.class, () -> service.redefinir(dto));
        verify(clienteRepository, never()).findById(5L);
        verify(recuperacaoRepository, never()).deleteByNivelAcessoAndUsuarioId(any(), any());
    }

    @Test
    void usaBloqueioExclusivoNaContaETokenParaSerializarRedefinicoes() throws Exception {
        assertEquals(LockModeType.PESSIMISTIC_WRITE, ClienteRepository.class
                .getMethod("findByIdForUpdate", Long.class).getAnnotation(org.springframework.data.jpa.repository.Lock.class).value());
        assertEquals(LockModeType.PESSIMISTIC_WRITE, BarbeiroRepository.class
                .getMethod("findByIdForUpdate", Long.class).getAnnotation(org.springframework.data.jpa.repository.Lock.class).value());
        assertEquals(LockModeType.PESSIMISTIC_WRITE, AdministradorRepository.class
                .getMethod("findByIdForUpdate", Long.class).getAnnotation(org.springframework.data.jpa.repository.Lock.class).value());
        assertEquals(LockModeType.PESSIMISTIC_WRITE, RecuperacaoSenhaRepository.class
                .getMethod("findByTokenHashForUpdate", String.class).getAnnotation(org.springframework.data.jpa.repository.Lock.class).value());
    }

    private ClienteEntity clienteAtivo() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(5L);
        cliente.setEmail("cliente@example.com");
        cliente.setStatus(Status.ATIVO);
        cliente.setSenha("senha-hash-antiga");
        return cliente;
    }

    private RecuperacaoSenhaEntity recuperacao(String token, LocalDateTime expiracao) throws Exception {
        RecuperacaoSenhaEntity recuperacao = new RecuperacaoSenhaEntity();
        recuperacao.setEmail("cliente@example.com");
        recuperacao.setUsuarioId(5L);
        recuperacao.setNivelAcesso(NivelAcesso.CLIENTE);
        recuperacao.setTokenHash(hash(token));
        recuperacao.setExpiraEm(expiracao);
        return recuperacao;
    }

    private String hash(String token) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
    }
}
