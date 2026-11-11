package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.CanalNotificacao;
import com.zheiry.barbersystem.enums.TipoNotificacao;
import com.zheiry.barbersystem.exception.FalhaEnvioNotificacaoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class NotificadoresHttpTest {

    @Test
    void resendEnviaComChaveDeIdempotenciaEGuardaIdDoProvedor() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        NotificacaoConfiguracao config = configResend();
        servidor.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token-de-teste"))
                .andExpect(header("Idempotency-Key", "notificacao:1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":\"resend-id\"}", MediaType.APPLICATION_JSON));

        var resultado = new NotificadorResend(config, builder).enviar(notificacao(CanalNotificacao.EMAIL));

        assertEquals("resend-id", resultado.identificadorProvedor());
        servidor.verify();
    }

    @Test
    void whatsappUsaEndpointGraphConfiguradoEModeloAprovado() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        NotificacaoConfiguracao config = configWhatsApp();
        servidor.expect(requestTo("https://graph.facebook.com/v-test/phone-id/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token-de-teste"))
                .andExpect(content().json("""
                        {"messaging_product":"whatsapp","recipient_type":"individual","to":"5511999999999","type":"template",
                         "template":{"name":"confirmacao_teste","language":{"code":"pt_BR"},"components":[{"type":"body","parameters":[
                         {"type":"text","text":"Cliente"},{"type":"text","text":"Barbearia"},{"type":"text","text":"Corte"},
                         {"type":"text","text":"Profissional"},{"type":"text","text":"25/10/2026 às 10:00"}]}]}}
                        """, true))
                .andRespond(withSuccess("{\"messages\":[{\"id\":\"wamid.test\"}]}", MediaType.APPLICATION_JSON));

        var resultado = new NotificadorWhatsAppCloud(config, builder).enviar(notificacao(CanalNotificacao.WHATSAPP));

        assertEquals("wamid.test", resultado.identificadorProvedor());
        servidor.verify();
    }

    @Test
    void faltaDeCredencialEhReportadaSemTentarChamada() {
        NotificadorResend notificador = new NotificadorResend(new NotificacaoConfiguracao(), RestClient.builder());

        FalhaEnvioNotificacaoException erro = assertThrows(FalhaEnvioNotificacaoException.class,
                () -> notificador.enviar(notificacao(CanalNotificacao.EMAIL)));

        assertFalse(erro.isRetentavel());
        assertFalse(erro.getMessage().contains("token"));
    }

    private NotificacaoConfiguracao configResend() {
        NotificacaoConfiguracao c = new NotificacaoConfiguracao();
        c.getResend().setAtivo(true);
        c.getResend().setApiKey("token-de-teste");
        c.getResend().setRemetente("Barbearia <contato@example.test>");
        return c;
    }

    private NotificacaoConfiguracao configWhatsApp() {
        NotificacaoConfiguracao c = new NotificacaoConfiguracao();
        var w = c.getWhatsapp();
        w.setAtivo(true);
        w.setVersaoGraph("v-test");
        w.setPhoneNumberId("phone-id");
        w.setTokenAcesso("token-de-teste");
        w.setCodigoPaisPadrao("55");
        for (TipoNotificacao tipo : TipoNotificacao.values()) {
            NotificacaoConfiguracao.ModeloMensagem m = new NotificacaoConfiguracao.ModeloMensagem();
            m.setNome(tipo == TipoNotificacao.CONFIRMACAO ? "confirmacao_teste" : tipo.name().toLowerCase());
            m.setIdioma("pt_BR");
            w.getModelos().put(tipo, m);
        }
        return c;
    }

    private NotificacaoEntity notificacao(CanalNotificacao canal) {
        NotificacaoEntity n = new NotificacaoEntity();
        n.setCanal(canal);
        n.setChaveIdempotencia("notificacao:1");
        n.setDestinatario("(11) 99999-9999");
        n.setNomeCliente("Cliente");
        n.setNomeBarbearia("Barbearia");
        n.setNomeServico("Corte");
        n.setNomeBarbeiro("Profissional");
        n.setTipo(TipoNotificacao.CONFIRMACAO);
        n.setDataHoraAgendamento(java.time.LocalDateTime.of(2026, 10, 25, 10, 0));
        return n;
    }
}
