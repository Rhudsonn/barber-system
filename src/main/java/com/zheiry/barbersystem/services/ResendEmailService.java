package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.dto.notificacao.ResultadoEnvio;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.TipoNotificacao;
import com.zheiry.barbersystem.exception.FalhaEnvioNotificacaoException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/** The only outbound transactional email provider used by the application. */
@Component
public class ResendEmailService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private final NotificacaoConfiguracao configuracao;
    private final RestClient clienteHttp;

    public ResendEmailService(NotificacaoConfiguracao configuracao, RestClient.Builder builder) {
        this.configuracao = configuracao;
        this.clienteHttp = builder.baseUrl("https://api.resend.com").build();
    }

    public ResultadoEnvio enviarNotificacao(NotificacaoEntity notificacao) {
        return enviarEmail(notificacao.getDestinatario(), assunto(notificacao), mensagem(notificacao),
                notificacao.getChaveIdempotencia());
    }

    public ResultadoEnvio enviarEmail(String destinatario, String assunto, String texto, String chaveIdempotencia) {
        var resend = configuracao.getResend();
        if (!"CONFIGURADA".equals(configuracao.estadoResend())) {
            throw new FalhaEnvioNotificacaoException("Integração de e-mail não configurada.", false);
        }
        try {
            Map<?, ?> resposta = clienteHttp.post().uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + resend.getApiKey())
                    .header("Idempotency-Key", chaveIdempotencia)
                    .body(Map.of("from", resend.getRemetente(), "to", destinatario,
                            "subject", assunto, "text", texto))
                    .retrieve().body(Map.class);
            Object id = resposta == null ? null : resposta.get("id");
            if (id == null) {
                throw new FalhaEnvioNotificacaoException("O provedor não confirmou o identificador do envio.", false);
            }
            return new ResultadoEnvio(id.toString());
        } catch (RestClientResponseException ex) {
            int codigo = ex.getStatusCode().value();
            throw new FalhaEnvioNotificacaoException(
                    "Falha HTTP no provedor de e-mail (" + codigo + ").", codigo == 429 || codigo >= 500);
        } catch (ResourceAccessException ex) {
            throw new FalhaEnvioNotificacaoException("Falha de comunicação com o provedor de e-mail.", true);
        }
    }

    private String assunto(NotificacaoEntity n) {
        return switch (n.getTipo()) {
            case CONFIRMACAO -> "Confirmação do seu agendamento";
            case LEMBRETE_2H, LEMBRETE_24H, LEMBRETE_1H, LEMBRETE_30MIN -> "Lembrete do seu agendamento";
            case CANCELAMENTO -> "Cancelamento do seu agendamento";
            case ANIVERSARIO -> "Feliz aniversário!";
        };
    }

    private String mensagem(NotificacaoEntity n) {
        if (n.getTipo() == TipoNotificacao.ANIVERSARIO) {
            return "Olá, " + n.getNomeCliente() + "! A equipe da " + n.getNomeBarbearia()
                    + " deseja a você um feliz aniversário, com muita saúde, alegria e ótimos momentos. Esperamos celebrar com você em breve!";
        }
        String verbo = n.getTipo() == TipoNotificacao.CANCELAMENTO
                ? "foi cancelado" : "está agendado";
        String aviso = n.getTipo() == TipoNotificacao.LEMBRETE_2H
                ? "Lembrete: faltam cerca de duas horas para o seu atendimento. " : "";
        return "Olá, " + n.getNomeCliente() + ". " + aviso + "Seu atendimento de " + n.getNomeServico() + " na "
                + n.getNomeBarbearia() + " com " + n.getNomeBarbeiro() + " " + verbo + " para "
                + DATA.format(n.getDataHoraAgendamento()) + ".";
    }
}
