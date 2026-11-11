package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.dto.notificacao.ResultadoEnvio;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.CanalNotificacao;
import com.zheiry.barbersystem.exception.FalhaEnvioNotificacaoException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class NotificadorResend implements CanalNotificador {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private final NotificacaoConfiguracao configuracao;
    private final RestClient clienteHttp;

    public NotificadorResend(NotificacaoConfiguracao configuracao, RestClient.Builder builder) {
        this.configuracao = configuracao;
        this.clienteHttp = builder.baseUrl("https://api.resend.com").build();
    }

    @Override
    public CanalNotificacao canal() { return CanalNotificacao.EMAIL; }

    @Override
    public ResultadoEnvio enviar(NotificacaoEntity notificacao) {
        var resend = configuracao.getResend();
        if (!"CONFIGURADA".equals(configuracao.estadoResend())) {
            throw new FalhaEnvioNotificacaoException("Integração de e-mail não configurada.", false);
        }
        String assunto = assunto(notificacao);
        String texto = mensagem(notificacao);
        try {
            Map<?, ?> resposta = clienteHttp.post().uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + resend.getApiKey())
                    .header("Idempotency-Key", notificacao.getChaveIdempotencia())
                    .body(Map.of("from", resend.getRemetente(), "to", notificacao.getDestinatario(),
                            "subject", assunto, "text", texto))
                    .retrieve().body(Map.class);
            Object id = resposta == null ? null : resposta.get("id");
            if (id == null) throw new FalhaEnvioNotificacaoException("O provedor não confirmou o identificador do envio.", false);
            return new ResultadoEnvio(id.toString());
        } catch (RestClientResponseException ex) {
            int codigo = ex.getStatusCode().value();
            throw new FalhaEnvioNotificacaoException("Falha HTTP no provedor de e-mail (" + codigo + ").", codigo == 429 || codigo >= 500);
        } catch (ResourceAccessException ex) {
            throw new FalhaEnvioNotificacaoException("Falha de comunicação com o provedor de e-mail.", true);
        }
    }

    private String assunto(NotificacaoEntity n) {
        return switch (n.getTipo()) {
            case CONFIRMACAO -> "Confirmação do seu agendamento";
            case LEMBRETE_24H, LEMBRETE_1H, LEMBRETE_30MIN -> "Lembrete do seu agendamento";
            case CANCELAMENTO -> "Cancelamento do seu agendamento";
        };
    }

    private String mensagem(NotificacaoEntity n) {
        String verbo = n.getTipo() == com.zheiry.barbersystem.enums.TipoNotificacao.CANCELAMENTO
                ? "foi cancelado" : "está agendado";
        return "Olá, " + n.getNomeCliente() + ". Seu atendimento de " + n.getNomeServico() + " na "
                + n.getNomeBarbearia() + " com " + n.getNomeBarbeiro() + " " + verbo + " para "
                + DATA.format(n.getDataHoraAgendamento()) + ".";
    }
}
