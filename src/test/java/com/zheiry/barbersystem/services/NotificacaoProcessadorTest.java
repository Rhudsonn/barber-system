package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
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
    @Mock ResendEmailService resendEmailService;
    private NotificacaoConfiguracao configuracao;
    private Clock relogio;

    @BeforeEach
    void preparar() {
        configuracao = new NotificacaoConfiguracao();
        relogio = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    }

    @Test
    void naoChamaProvedorQuandoIntegracaoEstaDesabilitada() {
        NotificacaoEntity n = notificacao("EMAIL");
        org.springframework.test.util.ReflectionTestUtils.setField(n, "id", 1L);
        when(service.buscarNotificacoesDevidas(any())).thenReturn(List.of(1L));
        when(service.reservar(eq(1L), any())).thenReturn(n);
        NotificacaoProcessador processador = new NotificacaoProcessador(service, configuracao, relogio, resendEmailService);

        processador.processarDevidas();

        verify(resendEmailService, never()).enviarNotificacao(any());
        verify(service).liberarReserva(eq(1L), any());
    }

    @Test
    void enviaERegistraIdentificadorDeProvedor() {
        configuracao.getResend().setAtivo(true);
        configuracao.getResend().setApiKey("segredo-apenas-no-teste");
        configuracao.getResend().setRemetente("Barbearia <contato@example.test>");
        NotificacaoEntity n = notificacao("EMAIL");
        when(service.buscarNotificacoesDevidas(any())).thenReturn(List.of(1L));
        when(service.reservar(eq(1L), any())).thenReturn(n);
        when(resendEmailService.enviarNotificacao(n)).thenReturn(new com.zheiry.barbersystem.dto.notificacao.ResultadoEnvio("email-id"));
        NotificacaoProcessador processador = new NotificacaoProcessador(service, configuracao, relogio, resendEmailService);

        processador.processarDevidas();

        verify(resendEmailService).enviarNotificacao(n);
        verify(service).registrarEnvio(eq(1L), eq(new com.zheiry.barbersystem.dto.notificacao.ResultadoEnvio("email-id")), any());
    }

    @Test
    void registraFalhaSemExporDetalhesDoProvedor() {
        configuracao.getResend().setAtivo(true);
        configuracao.getResend().setApiKey("segredo-apenas-no-teste");
        configuracao.getResend().setRemetente("Barbearia <contato@example.test>");
        NotificacaoEntity n = notificacao("EMAIL");
        when(service.buscarNotificacoesDevidas(any())).thenReturn(List.of(1L));
        when(service.reservar(eq(1L), any())).thenReturn(n);
        when(resendEmailService.enviarNotificacao(n)).thenThrow(new FalhaEnvioNotificacaoException("Timeout seguro", true));
        NotificacaoProcessador processador = new NotificacaoProcessador(service, configuracao, relogio, resendEmailService);

        processador.processarDevidas();

        verify(service).registrarFalha(1L, "Timeout seguro", true, LocalDateTime.now(relogio));
    }

    @Test
    void naoEnviaRegistroLegadoDeOutroCanal() {
        NotificacaoEntity legada = notificacao("CANAL_LEGADO");
        when(service.buscarNotificacoesDevidas(any())).thenReturn(List.of(1L));
        when(service.reservar(eq(1L), any())).thenReturn(legada);
        NotificacaoProcessador processador = new NotificacaoProcessador(
                service, configuracao, relogio, resendEmailService);

        processador.processarDevidas();

        verify(resendEmailService, never()).enviarNotificacao(any());
        verify(service).cancelarCanalDesativado(1L);
    }

    private NotificacaoEntity notificacao(String canal) {
        NotificacaoEntity n = new NotificacaoEntity();
        n.setCanal(canal);
        n.setStatus(StatusNotificacao.PROCESSANDO);
        n.setQuantidadeTentativas(1);
        return n;
    }
}
