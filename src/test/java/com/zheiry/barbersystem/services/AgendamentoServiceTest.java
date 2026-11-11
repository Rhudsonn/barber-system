package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.repositories.*;
import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock AgendamentoRepository agendamentoRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock BarbeiroRepository barbeiroRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock HorarioFuncionamentoRepository horarioRepository;
    @Mock BarbeariaRepository barbeariaRepository;
    @Mock BloqueioService bloqueioService;
    @Mock ComissaoService comissaoService;
    @Mock NotificacaoService notificacaoService;

    private AgendamentoService service;

    @BeforeEach
    void setUp() {
        service = new AgendamentoService(agendamentoRepository, clienteRepository, barbeiroRepository,
                servicoRepository, horarioRepository, barbeariaRepository, bloqueioService, comissaoService,
                notificacaoService, Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneId.of("America/Sao_Paulo")));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void conflitoDetectaAgendamentoQueComecouAntesDoIntervalo() {
        LocalDateTime inicioConsulta = LocalDateTime.of(2026, 10, 25, 10, 0);
        LocalDateTime fimConsulta = inicioConsulta.plusMinutes(30);
        AgendamentoEntity existente = new AgendamentoEntity();
        existente.setId(7L);
        existente.setDataHoraInicio(inicioConsulta.minusMinutes(15));
        existente.setDataHoraFim(inicioConsulta.plusMinutes(15));
        existente.setStatusAgendamento(StatusAgendamento.AGENDADO);
        when(agendamentoRepository.findByBarbeiroIdAndDataHoraInicioLessThanAndDataHoraFimGreaterThan(
                3L, fimConsulta, inicioConsulta)).thenReturn(List.of(existente));

        assertTrue(service.existeConflito(3L, inicioConsulta, fimConsulta, null));
    }

    @Test
    void permiteIntervalosSemAgendamentosSobrepostos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 25, 10, 0);
        LocalDateTime fim = inicio.plusMinutes(30);
        when(agendamentoRepository.findByBarbeiroIdAndDataHoraInicioLessThanAndDataHoraFimGreaterThan(
                3L, fim, inicio)).thenReturn(List.of());

        assertFalse(service.existeConflito(3L, inicio, fim, null));
        verify(bloqueioService).existeBloqueioNoPeriodo(3L, inicio, fim);
    }

    @Test
    void disponibilidadeRespeitaDuracaoDe45MinutosEPermiteApenasHorariosAdjacentes() {
        LocalDate data = LocalDate.of(2026, 10, 26);
        var barbearia = new com.zheiry.barbersystem.entities.BarbeariaEntity();
        barbearia.setId(1L);
        var faixa = new com.zheiry.barbersystem.entities.HorarioFuncionamentoEntity(
                1L, barbearia, com.zheiry.barbersystem.enums.DiaSemana.SEGUNDA,
                LocalTime.of(14, 0), LocalTime.of(17, 0));
        AgendamentoEntity agendado = agendamento(3L, 21L, StatusAgendamento.AGENDADO);
        agendado.setDataHoraInicio(data.atTime(15, 0));
        agendado.setDataHoraFim(data.atTime(15, 45));

        when(barbeiroRepository.existsById(3L)).thenReturn(true);
        when(barbeariaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(barbearia));
        when(horarioRepository.findByBarbeariaIdAndDiaSemana(1L,
                com.zheiry.barbersystem.enums.DiaSemana.SEGUNDA)).thenReturn(List.of(faixa));
        when(agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioLessThanAndDataHoraFimGreaterThanAndStatusAgendamentoNotIn(
                        eq(3L), eq(data.atTime(LocalTime.MAX)), eq(data.atStartOfDay()),
                        eq(List.of(StatusAgendamento.CANCELADO_CLIENTE, StatusAgendamento.CANCELADO_BARBEIRO))))
                .thenReturn(List.of(agendado));

        List<LocalTime> disponiveis = service.listarHorariosDisponiveis(3L, data, 45);

        assertTrue(disponiveis.contains(LocalTime.of(14, 15)),
                "Um serviço que termina exatamente às 15:00 pode anteceder a reserva existente.");
        assertFalse(disponiveis.contains(LocalTime.of(14, 30)),
                "Um horário que termina durante a reserva existente deve ser bloqueado.");
        assertFalse(disponiveis.contains(LocalTime.of(15, 0)), "O início da reserva existente está ocupado.");
        assertFalse(disponiveis.contains(LocalTime.of(15, 30)),
                "O serviço iniciado às 15:30 se sobrepõe até 15:45 e deve ser bloqueado.");
        assertTrue(disponiveis.contains(LocalTime.of(15, 45)),
                "Um serviço pode começar exatamente quando a reserva anterior termina.");
        assertTrue(disponiveis.contains(LocalTime.of(16, 15)),
                "O último início disponível deve terminar dentro do fechamento às 17:00.");
        assertFalse(disponiveis.contains(LocalTime.of(16, 30)),
                "Um serviço que ultrapassa o fechamento não deve ser oferecido.");
    }

    @Test
    void rejeitaDuracaoNaoPositivaNaConsultaDeDisponibilidade() {
        assertThrows(com.zheiry.barbersystem.exception.BadRequestException.class,
                () -> service.listarHorariosDisponiveis(3L, java.time.LocalDate.now().plusDays(1), 0));
        verifyNoInteractions(barbeiroRepository, barbeariaRepository, horarioRepository, agendamentoRepository);
    }

    @Test
    void clienteNaoPodeCancelarAgendamentoDeOutroCliente() {
        AgendamentoEntity agendamento = agendamento(21L, 21L, StatusAgendamento.AGENDADO);
        when(agendamentoRepository.findById(40L)).thenReturn(Optional.of(agendamento));
        autenticar(8L, "CLIENTE");

        assertThrows(AccessDeniedException.class, () -> service.cancelarPorCliente(40L));
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void clienteNaoPodeConsultarAgendamentoDeOutroCliente() {
        AgendamentoEntity agendamento = agendamento(21L, 21L, StatusAgendamento.AGENDADO);
        when(agendamentoRepository.findById(40L)).thenReturn(Optional.of(agendamento));
        autenticar(8L, "CLIENTE");

        assertThrows(AccessDeniedException.class, () -> service.buscarPorId(40L));
    }

    @Test
    void clienteNaoPodeListarAgendamentosDeOutroCliente() {
        autenticar(8L, "CLIENTE");

        assertThrows(AccessDeniedException.class,
                () -> service.listarPorCliente(21L, org.springframework.data.domain.Pageable.unpaged()));
        verifyNoInteractions(agendamentoRepository, clienteRepository);
    }

    @Test
    void clienteNaoPodeListarPeriodoDeOutroCliente() {
        autenticar(8L, "CLIENTE");

        assertThrows(AccessDeniedException.class, () -> service.listarPorClienteEPeriodo(
                21L, java.time.LocalDate.of(2026, 10, 1), java.time.LocalDate.of(2026, 10, 31)));
        verifyNoInteractions(agendamentoRepository, clienteRepository);
    }

    @Test
    void administradorPodeConsultarAgendamentosDeClienteNoPeriodo() {
        autenticar(1L, "ADMINISTRADOR");
        when(clienteRepository.existsById(21L)).thenReturn(true);
        when(agendamentoRepository.findByClienteIdAndDataHoraInicioGreaterThanEqualAndDataHoraInicioLessThanOrderByDataHoraInicioAsc(
                eq(21L), any(), any())).thenReturn(List.of());

        var resultado = service.listarPorClienteEPeriodo(21L,
                java.time.LocalDate.of(2026, 10, 1), java.time.LocalDate.of(2026, 10, 31));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void clienteNaoPodeCriarAgendamentoParaOutraConta() {
        autenticar(8L, "CLIENTE");
        var dto = new com.zheiry.barbersystem.dto.agendamento.AgendamentoDto(
                21L, 3L, 4L, LocalDateTime.of(2026, 10, 26, 10, 0), null);

        assertThrows(AccessDeniedException.class, () -> service.criar(dto));
        verifyNoInteractions(clienteRepository, barbeiroRepository, servicoRepository, agendamentoRepository);
    }

    @Test
    void barbeiroNaoPodeConsultarAgendaDeOutroBarbeiro() {
        autenticar(8L, "BARBEIRO");

        assertThrows(AccessDeniedException.class,
                () -> service.listarPorBarbeiro(21L, org.springframework.data.domain.Pageable.unpaged()));
        verifyNoInteractions(barbeiroRepository, agendamentoRepository);
    }

    @Test
    void rejeitaAgendamentoNoPassadoMesmoQuandoChamadoDiretamenteNoService() {
        autenticar(7L, "CLIENTE");
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(clienteAtivo(7L)));
        when(barbeiroRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(barbeiroAtivo(9L)));
        when(servicoRepository.findById(4L)).thenReturn(Optional.of(servicoAtivo(4L, 30)));
        var dto = new com.zheiry.barbersystem.dto.agendamento.AgendamentoDto(
                7L, 9L, 4L, LocalDateTime.of(2026, 10, 25, 8, 0), null);

        assertThrows(com.zheiry.barbersystem.exception.BadRequestException.class, () -> service.criar(dto));
        verifyNoInteractions(barbeariaRepository, horarioRepository, agendamentoRepository);
    }

    @Test
    void rejeitaAgendamentoForaDoHorarioDeFuncionamento() {
        autenticar(7L, "CLIENTE");
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(clienteAtivo(7L)));
        when(barbeiroRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(barbeiroAtivo(9L)));
        when(servicoRepository.findById(4L)).thenReturn(Optional.of(servicoAtivo(4L, 30)));
        var barbearia = new com.zheiry.barbersystem.entities.BarbeariaEntity();
        barbearia.setId(1L);
        when(barbeariaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(barbearia));
        when(horarioRepository.findByBarbeariaIdAndDiaSemana(1L, com.zheiry.barbersystem.enums.DiaSemana.SEGUNDA))
                .thenReturn(List.of());
        var dto = new com.zheiry.barbersystem.dto.agendamento.AgendamentoDto(
                7L, 9L, 4L, LocalDateTime.of(2026, 10, 26, 10, 0), null);

        assertThrows(com.zheiry.barbersystem.exception.BadRequestException.class, () -> service.criar(dto));
        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    void clienteNaoPodeReagendarAgendamentoDeOutroCliente() {
        AgendamentoEntity agendamento = agendamento(21L, 21L, StatusAgendamento.AGENDADO);
        when(agendamentoRepository.findById(40L)).thenReturn(Optional.of(agendamento));
        autenticar(8L, "CLIENTE");

        assertThrows(AccessDeniedException.class,
                () -> service.reagendar(40L, LocalDateTime.now().plusDays(2)));
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void barbeiroNaoPodeAlterarStatusDeAgendamentoDeOutroBarbeiro() {
        AgendamentoEntity agendamento = agendamento(21L, 8L, StatusAgendamento.AGENDADO);
        when(agendamentoRepository.findByIdForUpdate(40L)).thenReturn(Optional.of(agendamento));
        autenticar(99L, "BARBEIRO");

        assertThrows(AccessDeniedException.class,
                () -> service.atualizarStatus(40L, StatusAgendamento.EM_ANDAMENTO));
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void rejeitaTransicaoDeStatusInvalidaMesmoParaBarbeiroResponsavel() {
        AgendamentoEntity agendamento = agendamento(21L, 8L, StatusAgendamento.AGENDADO);
        when(agendamentoRepository.findByIdForUpdate(40L)).thenReturn(Optional.of(agendamento));
        autenticar(21L, "BARBEIRO");

        assertThrows(com.zheiry.barbersystem.exception.BadRequestException.class,
                () -> service.atualizarStatus(40L, StatusAgendamento.CONCLUIDO));
        verify(agendamentoRepository, never()).save(any());
    }

    private AgendamentoEntity agendamento(Long barbeiroId, Long clienteId, StatusAgendamento status) {
        com.zheiry.barbersystem.entities.BarbeiroEntity barbeiro = new com.zheiry.barbersystem.entities.BarbeiroEntity();
        barbeiro.setId(barbeiroId);
        com.zheiry.barbersystem.entities.ClienteEntity cliente = new com.zheiry.barbersystem.entities.ClienteEntity();
        cliente.setId(clienteId);
        AgendamentoEntity entity = new AgendamentoEntity();
        entity.setId(40L);
        entity.setBarbeiro(barbeiro);
        entity.setCliente(cliente);
        entity.setStatusAgendamento(status);
        return entity;
    }

    private com.zheiry.barbersystem.entities.ClienteEntity clienteAtivo(Long id) {
        var cliente = new com.zheiry.barbersystem.entities.ClienteEntity();
        cliente.setId(id);
        cliente.setStatus(Status.ATIVO);
        return cliente;
    }

    private com.zheiry.barbersystem.entities.BarbeiroEntity barbeiroAtivo(Long id) {
        var barbeiro = new com.zheiry.barbersystem.entities.BarbeiroEntity();
        barbeiro.setId(id);
        barbeiro.setStatus(Status.ATIVO);
        return barbeiro;
    }

    private com.zheiry.barbersystem.entities.ServicoEntity servicoAtivo(Long id, int duracao) {
        var servico = new com.zheiry.barbersystem.entities.ServicoEntity();
        servico.setId(id);
        servico.setStatus(Status.ATIVO);
        servico.setDuracaoEmMinutos(duracao);
        return servico;
    }

    private void autenticar(Long id, String role) {
        UsuarioAutenticado principal = new UsuarioAutenticado(id, "user@example.com", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_" + role)), true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
