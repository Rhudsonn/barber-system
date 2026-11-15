package com.zheiry.barbersystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@ConfigurationProperties(prefix = "app.notificacoes")
public class NotificacaoConfiguracao {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    private final Resend resend = new Resend();
    private int maxTentativas = 5;
    private long intervaloMs = 30000;
    private int leaseMinutos = 5;

    public Resend getResend() { return resend; }
    public int getMaxTentativas() { return maxTentativas; }
    public void setMaxTentativas(int maxTentativas) { this.maxTentativas = maxTentativas; }
    public long getIntervaloMs() { return intervaloMs; }
    public void setIntervaloMs(long intervaloMs) { this.intervaloMs = intervaloMs; }
    public int getLeaseMinutos() { return leaseMinutos; }
    public void setLeaseMinutos(int leaseMinutos) { this.leaseMinutos = leaseMinutos; }

    public String estadoResend() {
        if (!resend.ativo) return "DESABILITADA";
        return resend.apiKey != null && !resend.apiKey.isBlank()
                && resend.remetente != null && !resend.remetente.isBlank() ? "CONFIGURADA" : "NAO_CONFIGURADA";
    }

    public static class Resend {
        private boolean ativo;
        private String apiKey;
        private String remetente;
        public boolean isAtivo() { return ativo; }
        public void setAtivo(boolean ativo) { this.ativo = ativo; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getRemetente() { return remetente; }
        public void setRemetente(String remetente) { this.remetente = remetente; }
    }

}
