package com.zheiry.barbersystem.config;

import com.zheiry.barbersystem.enums.TipoNotificacao;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.EnumMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.notificacoes")
public class NotificacaoConfiguracao {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    private final Resend resend = new Resend();
    private final WhatsApp whatsapp = new WhatsApp();
    private int maxTentativas = 5;
    private long intervaloMs = 30000;
    private int leaseMinutos = 5;

    public Resend getResend() { return resend; }
    public WhatsApp getWhatsapp() { return whatsapp; }
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

    public String estadoWhatsApp() {
        if (!whatsapp.ativo) return "DESABILITADA";
        boolean configurado = presente(whatsapp.urlBase) && presente(whatsapp.versaoGraph)
                && presente(whatsapp.phoneNumberId) && presente(whatsapp.tokenAcesso)
                && presente(whatsapp.codigoPaisPadrao)
                && whatsapp.modelos.size() == TipoNotificacao.values().length
                && whatsapp.modelos.values().stream().allMatch(m -> presente(m.nome) && presente(m.idioma));
        return configurado ? "CONFIGURADA" : "NAO_CONFIGURADA";
    }

    private boolean presente(String valor) { return valor != null && !valor.isBlank(); }

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

    public static class WhatsApp {
        private boolean ativo;
        private String urlBase = "https://graph.facebook.com";
        private String versaoGraph;
        private String phoneNumberId;
        private String businessAccountId;
        private String tokenAcesso;
        private String codigoPaisPadrao;
        private Map<TipoNotificacao, ModeloMensagem> modelos = new EnumMap<>(TipoNotificacao.class);
        public boolean isAtivo() { return ativo; }
        public void setAtivo(boolean ativo) { this.ativo = ativo; }
        public String getUrlBase() { return urlBase; }
        public void setUrlBase(String urlBase) { this.urlBase = urlBase; }
        public String getVersaoGraph() { return versaoGraph; }
        public void setVersaoGraph(String versaoGraph) { this.versaoGraph = versaoGraph; }
        public String getPhoneNumberId() { return phoneNumberId; }
        public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }
        public String getBusinessAccountId() { return businessAccountId; }
        public void setBusinessAccountId(String businessAccountId) { this.businessAccountId = businessAccountId; }
        public String getTokenAcesso() { return tokenAcesso; }
        public void setTokenAcesso(String tokenAcesso) { this.tokenAcesso = tokenAcesso; }
        public String getCodigoPaisPadrao() { return codigoPaisPadrao; }
        public void setCodigoPaisPadrao(String codigoPaisPadrao) { this.codigoPaisPadrao = codigoPaisPadrao; }
        public Map<TipoNotificacao, ModeloMensagem> getModelos() { return modelos; }
        public void setModelos(Map<TipoNotificacao, ModeloMensagem> modelos) { this.modelos = modelos; }
    }

    public static class ModeloMensagem {
        private String nome;
        private String idioma;
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getIdioma() { return idioma; }
        public void setIdioma(String idioma) { this.idioma = idioma; }
    }
}
