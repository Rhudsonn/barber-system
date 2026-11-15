package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
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

        var resultado = new ResendEmailService(config, builder)
                .enviarNotificacao(notificacao("EMAIL"));

        assertEquals("resend-id", resultado.identificadorProvedor());
        servidor.verify();
    }

    @Test
    void recuperacaoDeSenhaUsaOMesmoProvedorResendComChaveIdempotente() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        NotificacaoConfiguracao config = configResend();
        servidor.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token-de-teste"))
                .andExpect(header("Idempotency-Key", "recuperacao-senha:8"))
                .andExpect(content().json("""
                        {"from":"Barbearia <contato@example.test>","to":"cliente@example.test",
                         "subject":"Redefinição de senha","text":"Use o link de recuperação."}
                        """, true))
                .andRespond(withSuccess("{\"id\":\"resend-recovery-id\"}", MediaType.APPLICATION_JSON));

        var resultado = new ResendEmailService(config, builder).enviarEmail(
                "cliente@example.test", "Redefinição de senha", "Use o link de recuperação.", "recuperacao-senha:8");

        assertEquals("resend-recovery-id", resultado.identificadorProvedor());
        servidor.verify();
    }

    @Test
    void faltaDeCredencialEhReportadaSemTentarChamada() {
        ResendEmailService notificador = new ResendEmailService(new NotificacaoConfiguracao(), RestClient.builder());

        FalhaEnvioNotificacaoException erro = assertThrows(FalhaEnvioNotificacaoException.class,
                () -> notificador.enviarNotificacao(notificacao("EMAIL")));

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

    private NotificacaoEntity notificacao(String canal) {
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
