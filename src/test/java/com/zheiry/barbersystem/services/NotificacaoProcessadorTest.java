package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.dto.notificacao.ResultadoEnvio;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.CanalNotificacao;
import com.zheiry.barbersystem.enums.StatusNotificacao;
import com.zheiry.barbersystem.exception.FalhaEnvioNotificacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoProcessadorTest {
    @Mock NotificacaoService service;
    @Mock CanalNotificador email;
    @Mock CanalNotificador whatsapp;
    private NotificacaoConfiguracao configuracao;
    private Clock relogio;

    @BeforeEach
    void preparar() {
        configuracao = new NotificacaoConfiguracao();
        relogio = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        when(email.canal()).thenReturn(CanalNotificacao.EMAIL);
        when(whatsapp.canal()).thenReturn(CanalNotificacao.WHATSAPP);
    }

    @Test
    void naoChamaProvedorQuandoIntegracaoEstaDesabilitada() {
        NotificacaoEntity n = notificacao(CanalNotificacao.EMAIL);
        org.springframework.test.util.ReflectionTestUtils.setField(n, "id", 1L);
        when(service.buscarNotificacoesDevidas(any())).thenReturn(List.of(1L));
        when(service.reservar(eq(1L), any())).thenReturn(n);
        NotificacaoProcessador processador = new NotificacaoProcessador(service, configuracao, relogio, List.of(email, whatsapp));

        processador.processarDevidas();

        verify(email, never()).enviar(any());
        verify(whatsapp, never()).enviar(any());
        verify(service).liberarReserva(eq(1L), any());
    }

    @Test
    void enviaERegistraIdentificadorDeProvedor() {
        configuracao.getResend().setAtivo(true);
        configuracao.getResend().setApiKey("segredo-apenas-no-teste");
        configuracao.getResend().setRemetente("Barbearia <contato@example.test>");
        NotificacaoEntity n = notificacao(CanalNotificacao.EMAIL);
        when(service.buscarNotificacoesDevidas(any())).thenReturn(List.of(1L));
        when(service.reservar(eq(1L), any())).thenReturn(n);
        when(email.enviar(n)).thenReturn(new ResultadoEnvio("email-id"));
        NotificacaoProcessador processador = new NotificacaoProcessador(service, configuracao, relogio, List.of(email, whatsapp));

        processador.processarDevidas();

        verify(email).enviar(n);
        verify(service).registrarEnvio(eq(1L), eq(new ResultadoEnvio("email-id")), any());
    }

    @Test
    void registraFalhaSemExporDetalhesDoProvedor() {
        configuracao.getResend().setAtivo(true);
        configuracao.getResend().setApiKey("segredo-apenas-no-teste");
        configuracao.getResend().setRemetente("Barbearia <contato@example.test>");
        NotificacaoEntity n = notificacao(CanalNotificacao.EMAIL);
        when(service.buscarNotificacoesDevidas(any())).thenReturn(List.of(1L));
        when(service.reservar(eq(1L), any())).thenReturn(n);
        when(email.enviar(n)).thenThrow(new FalhaEnvioNotificacaoException("Timeout seguro", true));
        NotificacaoProcessador processador = new NotificacaoProcessador(service, configuracao, relogio, List.of(email, whatsapp));

        processador.processarDevidas();

        verify(service).registrarFalha(1L, "Timeout seguro", true, LocalDateTime.now(relogio));
    }

    private NotificacaoEntity notificacao(CanalNotificacao canal) {
        NotificacaoEntity n = new NotificacaoEntity();
        n.setCanal(canal);
        n.setStatus(StatusNotificacao.PROCESSANDO);
        n.setQuantidadeTentativas(1);
        return n;
    }
}
