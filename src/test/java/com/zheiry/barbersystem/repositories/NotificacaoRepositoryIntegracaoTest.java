package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.CanalNotificacao;
import com.zheiry.barbersystem.enums.StatusNotificacao;
import com.zheiry.barbersystem.enums.TipoNotificacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notificacoes;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class NotificacaoRepositoryIntegracaoTest {
    @Autowired NotificacaoRepository repository;

    @Test
    void persisteEstadosEReservaUmaNotificacaoVencidaUmaUnicaVez() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 25, 10, 0);
        NotificacaoEntity notificacao = notificacao("chave-unica", agora.minusMinutes(1));
        repository.saveAndFlush(notificacao);

        var ids = repository.buscarIdsDevidas(StatusNotificacao.PENDENTE, StatusNotificacao.PROCESSANDO,
                agora, agora.minusMinutes(5), PageRequest.of(0, 20));

        assertEquals(1, ids.size());
        assertEquals(1, repository.reservar(ids.getFirst(), StatusNotificacao.PENDENTE,
                StatusNotificacao.PROCESSANDO, agora, agora.minusMinutes(5)));
        assertEquals(0, repository.reservar(ids.getFirst(), StatusNotificacao.PENDENTE,
                StatusNotificacao.PROCESSANDO, agora, agora.minusMinutes(5)));
        assertEquals(StatusNotificacao.PROCESSANDO, repository.findById(notificacao.getId()).orElseThrow().getStatus());
    }

    @Test
    void rejeitaDuasNotificacoesComMesmaChaveDeIdempotencia() {
        LocalDateTime agora = LocalDateTime.now();
        repository.saveAndFlush(notificacao("idempotencia-repetida", agora));

        assertThrows(RuntimeException.class, () -> repository.saveAndFlush(notificacao("idempotencia-repetida", agora)));
    }

    private NotificacaoEntity notificacao(String chave, LocalDateTime programada) {
        NotificacaoEntity n = new NotificacaoEntity();
        n.setClienteId(1L);
        n.setAgendamentoId(2L);
        n.setTipo(TipoNotificacao.LEMBRETE_1H);
        n.setCanal(CanalNotificacao.EMAIL);
        n.setStatus(StatusNotificacao.PENDENTE);
        n.setChaveIdempotencia(chave);
        n.setDestinatario("cliente@example.test");
        n.setNomeCliente("Cliente");
        n.setNomeBarbearia("Barbearia");
        n.setNomeServico("Corte");
        n.setNomeBarbeiro("Barbeiro");
        n.setDataHoraAgendamento(programada.plusHours(1));
        n.setProgramadaPara(programada);
        n.setCriadaEm(programada.minusDays(1));
        return n;
    }
}
