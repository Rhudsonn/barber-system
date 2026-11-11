package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.exception.FalhaEnvioNotificacaoException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificacaoProcessador {
    private final NotificacaoService service;
    private final NotificacaoConfiguracao configuracao;
    private final Clock relogio;
    private final Map<com.zheiry.barbersystem.enums.CanalNotificacao, CanalNotificador> notificadores;

    public NotificacaoProcessador(NotificacaoService service, NotificacaoConfiguracao configuracao,
                                  Clock relogio, List<CanalNotificador> notificadores) {
        this.service = service;
        this.configuracao = configuracao;
        this.relogio = relogio;
        this.notificadores = notificadores.stream().collect(Collectors.toMap(CanalNotificador::canal, Function.identity()));
    }

    @Scheduled(fixedDelayString = "${app.notificacoes.intervalo-ms:30000}")
    public void processarDevidas() {
        LocalDateTime agora = LocalDateTime.now(relogio);
        for (Long id : service.buscarNotificacoesDevidas(agora)) {
            NotificacaoEntity notificacao = service.reservar(id, agora);
            if (notificacao == null) continue;
            String estado = notificacao.getCanal() == com.zheiry.barbersystem.enums.CanalNotificacao.EMAIL
                    ? configuracao.estadoResend() : configuracao.estadoWhatsApp();
            // Desabilitada ou sem configuração: conserva a fila pendente e não faz chamada externa.
            if (!"CONFIGURADA".equals(estado)) {
                service.liberarReserva(notificacao.getId(), agora);
                continue;
            }
            try {
                CanalNotificador notificador = notificadores.get(notificacao.getCanal());
                if (notificador == null) throw new FalhaEnvioNotificacaoException("Canal de notificação sem implementação.", false);
                service.registrarEnvio(id, notificador.enviar(notificacao), LocalDateTime.now(relogio));
            } catch (FalhaEnvioNotificacaoException ex) {
                service.registrarFalha(id, ex.getMessage(), ex.isRetentavel(), LocalDateTime.now(relogio));
            } catch (RuntimeException ex) {
                // Não persistir detalhes da exceção para evitar vazamento de credenciais ou payloads.
                service.registrarFalha(id, "Falha inesperada ao processar notificação.", true, LocalDateTime.now(relogio));
            }
        }
    }
}
