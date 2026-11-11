package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.dto.notificacao.PreferenciaNotificacaoAtualizaDto;
import com.zheiry.barbersystem.entities.*;
import com.zheiry.barbersystem.enums.CanalNotificacao;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.enums.StatusNotificacao;
import com.zheiry.barbersystem.enums.TipoNotificacao;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.repositories.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZONA);
    @Mock NotificacaoRepository notificacaoRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock BarbeariaRepository barbeariaRepository;
    @Mock AgendamentoRepository agendamentoRepository;
    private NotificacaoConfiguracao configuracao;
    private NotificacaoService service;

    @BeforeEach
    void setUp() {
        configuracao = new NotificacaoConfiguracao();
        service = new NotificacaoService(notificacaoRepository, clienteRepository, barbeariaRepository,
                agendamentoRepository, configuracao, RELOGIO);
    }

    @AfterEach
    void limparAutenticacao() { SecurityContextHolder.clearContext(); }

    @Test
    void consultaPreferenciaDoClienteAutenticadoSemReceberIdDoRequest() {
        ClienteEntity cliente = cliente(7L);
        cliente.setCanalNotificacao(CanalNotificacao.EMAIL);
        cliente.setNotificacoesAutorizadas(true);
        autenticar(7L);
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));

        var dto = service.consultarPreferencia();

        assertEquals(CanalNotificacao.EMAIL, dto.canal());
        assertTrue(dto.autorizada());
    }

    @Test
    void atualizaPreferenciaApenasDoClienteAutenticado() {
        ClienteEntity cliente = cliente(7L);
        autenticar(7L);
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));

        var dto = service.atualizarPreferencia(new PreferenciaNotificacaoAtualizaDto(CanalNotificacao.WHATSAPP, true));

        assertEquals(CanalNotificacao.WHATSAPP, cliente.getCanalNotificacao());
        assertTrue(cliente.isNotificacoesAutorizadas());
        verify(clienteRepository).save(cliente);
        assertEquals(7L, cliente.getId());
        assertEquals(CanalNotificacao.WHATSAPP, dto.canal());
    }

    @Test
    void rejeitaWhatsAppSemTelefone() {
        ClienteEntity cliente = cliente(7L);
        cliente.setTelefone(" ");
        autenticar(7L);
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));

        assertThrows(BadRequestException.class,
                () -> service.atualizarPreferencia(new PreferenciaNotificacaoAtualizaDto(CanalNotificacao.WHATSAPP, true)));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void programaTresLembretesPersistidosQuandoClienteAutorizou() {
        AgendamentoEntity agendamento = agendamento(9L, 7L, java.time.LocalDateTime.now(RELOGIO).plusDays(2));
        when(barbeariaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(barbearia()));
        when(notificacaoRepository.findByChaveIdempotencia(anyString())).thenReturn(Optional.empty());

        service.agendamentoCriado(agendamento);

        ArgumentCaptor<NotificacaoEntity> captor = ArgumentCaptor.forClass(NotificacaoEntity.class);
        verify(notificacaoRepository, times(4)).save(captor.capture());
        assertEquals(1, captor.getAllValues().stream().filter(n -> n.getTipo() == TipoNotificacao.CONFIRMACAO).count());
        assertEquals(3, captor.getAllValues().stream().filter(n -> n.getTipo().name().startsWith("LEMBRETE_")).count());
        assertTrue(captor.getAllValues().stream().allMatch(n -> n.getStatus() == StatusNotificacao.PENDENTE));
    }

    @Test
    void naoEnviaNemCriaLembreteComHorarioQueJaPassou() {
        AgendamentoEntity agendamento = agendamento(9L, 7L, java.time.LocalDateTime.now(RELOGIO).plusMinutes(20));
        when(barbeariaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(barbearia()));
        when(notificacaoRepository.findByChaveIdempotencia(anyString())).thenReturn(Optional.empty());
        java.util.List<StatusNotificacao> statusSalvos = new java.util.ArrayList<>();
        when(notificacaoRepository.save(any(NotificacaoEntity.class)))
                .thenAnswer(invocacao -> {
                    NotificacaoEntity notificacao = invocacao.getArgument(0);
                    statusSalvos.add(notificacao.getStatus());
                    return notificacao;
                });

        service.agendamentoCriado(agendamento);

        verify(notificacaoRepository, times(7)).save(any(NotificacaoEntity.class));
        assertEquals(3, statusSalvos.stream().filter(status -> status == StatusNotificacao.CANCELADA).count());
        assertEquals(4, statusSalvos.stream().filter(status -> status == StatusNotificacao.PENDENTE).count());
    }

    @Test
    void naoDuplicaLembretesOuConfirmacaoQuandoChaveJaExiste() {
        AgendamentoEntity agendamento = agendamento(9L, 7L, java.time.LocalDateTime.now(RELOGIO).plusDays(2));
        NotificacaoEntity existente = new NotificacaoEntity();
        when(notificacaoRepository.findByChaveIdempotencia(anyString())).thenReturn(Optional.of(existente));

        service.agendamentoConfirmado(agendamento);

        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void cancelamentoInvalidaLembretesPendentesERegistraEvento() {
        AgendamentoEntity agendamento = agendamento(9L, 7L, java.time.LocalDateTime.now(RELOGIO).plusDays(2));
        when(barbeariaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(barbearia()));
        when(notificacaoRepository.findByChaveIdempotencia(anyString())).thenReturn(Optional.empty());

        service.agendamentoCancelado(agendamento);

        verify(notificacaoRepository).cancelarPendentes(eq(9L), eq(StatusNotificacao.PENDENTE),
                eq(StatusNotificacao.CANCELADA), anyList(), contains("cancelado"));
        verify(notificacaoRepository).save(argThat(n -> n.getTipo() == TipoNotificacao.CANCELAMENTO));
    }

    @Test
    void desativarConsentimentoCancelaFilaAindaPendente() {
        ClienteEntity cliente = cliente(7L);
        cliente.setCanalNotificacao(CanalNotificacao.EMAIL);
        autenticar(7L);
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));
        NotificacaoEntity pendente = new NotificacaoEntity();
        when(notificacaoRepository.findByClienteIdAndStatus(7L, StatusNotificacao.PENDENTE)).thenReturn(List.of(pendente));

        service.atualizarPreferencia(new PreferenciaNotificacaoAtualizaDto(CanalNotificacao.EMAIL, false));

        assertEquals(StatusNotificacao.CANCELADA, pendente.getStatus());
        assertTrue(pendente.getErroSeguro().contains("desativada"));
    }

    private ClienteEntity cliente(Long id) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(id);
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@example.test");
        cliente.setTelefone("11999999999");
        cliente.setNotificacoesAutorizadas(true);
        cliente.setCanalNotificacao(CanalNotificacao.EMAIL);
        return cliente;
    }

    private AgendamentoEntity agendamento(Long id, Long clienteId, java.time.LocalDateTime inicio) {
        AgendamentoEntity agendamento = new AgendamentoEntity();
        agendamento.setId(id);
        agendamento.setCliente(cliente(clienteId));
        BarbeiroEntity barbeiro = new BarbeiroEntity();
        barbeiro.setId(12L);
        barbeiro.setNome("Barbeiro Teste");
        agendamento.setBarbeiro(barbeiro);
        ServicoEntity servico = new ServicoEntity();
        servico.setId(15L);
        servico.setNome("Corte");
        agendamento.setServico(servico);
        agendamento.setDataHoraInicio(inicio);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);
        return agendamento;
    }

    private BarbeariaEntity barbearia() {
        BarbeariaEntity barbearia = new BarbeariaEntity();
        barbearia.setNomeFantasia("Barbearia Teste");
        return barbearia;
    }

    private void autenticar(Long id) {
        UsuarioAutenticado usuario = new UsuarioAutenticado(id, "cliente@example.test", "hash",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")), true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
    }
}
