package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.exception.FalhaEnvioNotificacaoException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class NotificacaoProcessador {
    private final NotificacaoService service;
    private final NotificacaoConfiguracao configuracao;
    private final Clock relogio;
    private final ResendEmailService resendEmailService;

    public NotificacaoProcessador(NotificacaoService service, NotificacaoConfiguracao configuracao,
                                  Clock relogio, ResendEmailService resendEmailService) {
        this.service = service;
        this.configuracao = configuracao;
        this.relogio = relogio;
        this.resendEmailService = resendEmailService;
    }

    @Scheduled(fixedDelayString = "${app.notificacoes.intervalo-ms:30000}")
    public void processarDevidas() {
        LocalDateTime agora = LocalDateTime.now(relogio);
        for (Long id : service.buscarNotificacoesDevidas(agora)) {
            NotificacaoEntity notificacao = service.reservar(id, agora);
            if (notificacao == null) continue;
            if (!"EMAIL".equalsIgnoreCase(notificacao.getCanal())) {
                service.cancelarCanalDesativado(id);
                continue;
            }
            String estado = configuracao.estadoResend();
            // Desabilitada ou sem configuração: conserva a fila pendente e não faz chamada externa.
            if (!"CONFIGURADA".equals(estado)) {
                service.liberarReserva(notificacao.getId(), agora);
                continue;
            }
            try {
                service.registrarEnvio(id, resendEmailService.enviarNotificacao(notificacao), LocalDateTime.now(relogio));
            } catch (FalhaEnvioNotificacaoException ex) {
                service.registrarFalha(id, ex.getMessage(), ex.isRetentavel(), LocalDateTime.now(relogio));
            } catch (RuntimeException ex) {
                // Não persistir detalhes da exceção para evitar vazamento de credenciais ou payloads.
                service.registrarFalha(id, "Falha inesperada ao processar notificação.", true, LocalDateTime.now(relogio));
            }
        }
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "${app.time-zone:America/Sao_Paulo}")
    public void revisarLembretesAgendados() { service.revisarLembretesFuturos(); }

    @Scheduled(cron = "0 0 9 * * *", zone = "${app.time-zone:America/Sao_Paulo}")
    public void enviarAniversarios() { service.programarAniversariantes(); }
}
