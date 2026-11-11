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
import java.util.List;
import java.util.Map;

@Component
public class NotificadorWhatsAppCloud implements CanalNotificador {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private final NotificacaoConfiguracao configuracao;
    private final RestClient clienteHttp;

    public NotificadorWhatsAppCloud(NotificacaoConfiguracao configuracao, RestClient.Builder builder) {
        this.configuracao = configuracao;
        this.clienteHttp = builder.build();
    }

    @Override
    public CanalNotificacao canal() { return CanalNotificacao.WHATSAPP; }

    @Override
    public ResultadoEnvio enviar(NotificacaoEntity notificacao) {
        var whatsapp = configuracao.getWhatsapp();
        if (!"CONFIGURADA".equals(configuracao.estadoWhatsApp())) {
            throw new FalhaEnvioNotificacaoException("Integração WhatsApp não configurada.", false);
        }
        var modelo = whatsapp.getModelos().get(notificacao.getTipo());
        Map<String, Object> conteudoModelo = Map.of(
                "name", modelo.getNome(),
                "language", Map.of("code", modelo.getIdioma()),
                "components", List.of(Map.of("type", "body", "parameters", List.of(
                        parametro(notificacao.getNomeCliente()), parametro(notificacao.getNomeBarbearia()),
                        parametro(notificacao.getNomeServico()), parametro(notificacao.getNomeBarbeiro()),
                        parametro(DATA.format(notificacao.getDataHoraAgendamento()))))));
        Map<String, Object> corpo = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", telefoneInternacional(notificacao.getDestinatario(), whatsapp.getCodigoPaisPadrao()),
                "type", "template",
                "template", conteudoModelo);

        try {
            String caminho = "/" + whatsapp.getVersaoGraph() + "/" + whatsapp.getPhoneNumberId() + "/messages";
            Map<?, ?> resposta = clienteHttp.post().uri(whatsapp.getUrlBase() + caminho)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + whatsapp.getTokenAcesso())
                    .body(corpo).retrieve().body(Map.class);
            Object mensagens = resposta == null ? null : resposta.get("messages");
            if (!(mensagens instanceof List<?> lista) || lista.isEmpty() || !(lista.get(0) instanceof Map<?, ?> item)
                    || item.get("id") == null) {
                throw new FalhaEnvioNotificacaoException("O provedor WhatsApp não confirmou o identificador do envio.", false);
            }
            return new ResultadoEnvio(item.get("id").toString());
        } catch (RestClientResponseException ex) {
            int codigo = ex.getStatusCode().value();
            throw new FalhaEnvioNotificacaoException("Falha HTTP na API WhatsApp (" + codigo + ").", codigo == 429 || codigo >= 500);
        } catch (ResourceAccessException ex) {
            throw new FalhaEnvioNotificacaoException("Falha de comunicação com a API WhatsApp.", true);
        }
    }

    private Map<String, String> parametro(String texto) { return Map.of("type", "text", "text", texto); }

    private String telefoneInternacional(String telefone, String codigoPais) {
        String digitos = telefone.replaceAll("\\D", "");
        String pais = codigoPais.replaceAll("\\D", "");
        return digitos.length() <= 11 && !digitos.startsWith(pais) ? pais + digitos : digitos;
    }
}
