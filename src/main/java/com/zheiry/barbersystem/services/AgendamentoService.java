package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.agendamento.AgendamentoDto;
import com.zheiry.barbersystem.dto.agendamento.AgendamentoSaidaDto;
import com.zheiry.barbersystem.entities.*;
import com.zheiry.barbersystem.enums.DiaSemana;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.AgendamentoMapper;
import com.zheiry.barbersystem.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final ServicoRepository servicoRepository;
    private final HorarioFuncionamentoRepository horarioFuncionamentoRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final BloqueioService bloqueioService;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository, BarbeiroRepository barbeiroRepository, ServicoRepository servicoRepository, HorarioFuncionamentoRepository horarioFuncionamentoRepository, BarbeariaRepository barbeariaRepository, BloqueioService bloqueioService) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.servicoRepository = servicoRepository;
        this.horarioFuncionamentoRepository = horarioFuncionamentoRepository;
        this.barbeariaRepository = barbeariaRepository;
        this.bloqueioService = bloqueioService;
    }

    // Verifica se existe conflito de horário para o barbeiro.
    //Busca os agendamentos do dia e verifica a sobreposição em memória.
    public boolean existeConflito(Long barbeiroId, LocalDateTime inicio, LocalDateTime fim, Long agendamentoIdExcluido) {

        // Verifica se existe bloqueio no periodo
        if (bloqueioService.existeBloqueioNoPeriodo(barbeiroId, inicio, fim)){
            return true;
        }

        // Verifica conflito com outros agendamentos
        LocalDate data = inicio.toLocalDate();
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        List<AgendamentoEntity> agendamentos = agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioBetween(barbeiroId, inicioDoDia, fimDoDia);

        return agendamentos.stream()
                .filter(a -> a.getStatusAgendamento() != StatusAgendamento.CANCELADO_CLIENTE
                        && a.getStatusAgendamento() != StatusAgendamento.CANCELADO_BARBEIRO)
                .filter(a -> agendamentoIdExcluido == null || !a.getId().equals(agendamentoIdExcluido))
                .anyMatch(a -> a.getDataHoraInicio().isBefore(fim)
                        && a.getDataHoraFim().isAfter(inicio));
    }


    // Criar agendamento
    // Valida cliente, barbeiro e serviço, calcula o horário de fim, e verifica se há conflito de horário.
    @Transactional
    public AgendamentoSaidaDto criar(AgendamentoDto dto) throws BadRequestException {

        ClienteEntity cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        if (cliente.getStatus() != Status.ATIVO) {
            throw new BadRequestException("O cliente está inativo e não pode realizar agendamentos.");
        }

        BarbeiroEntity barbeiro = barbeiroRepository.findById(dto.barbeiroId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado."));

        if (barbeiro.getStatus() != Status.ATIVO) {
            throw new BadRequestException("O barbeiro está inativo e não pode receber agendamentos.");
        }

        ServicoEntity servico = servicoRepository.findById(dto.servicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        if (servico.getStatus() != Status.ATIVO) {
            throw new BadRequestException("O serviço está inativo e não pode ser agendado.");
        }

        LocalDateTime dataHoraInicio = dto.dataHoraInicio();
        LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(servico.getDuracaoEmMinutos());

        // Verifica conflito de horário
        if (existeConflito(barbeiro.getId(), dataHoraInicio, dataHoraFim, null)) {
            throw new BadRequestException("Já existe um agendamento ou bloqueio para este barbeiro neste horário.");
        }

        AgendamentoEntity entity = AgendamentoMapper.toEntity(dto, cliente, barbeiro, servico);
        AgendamentoEntity salvo = agendamentoRepository.save(entity);

        return AgendamentoMapper.toSaidaDto(salvo);
    }


    // Buscar agendamento por ID
    public AgendamentoSaidaDto buscarPorId(Long id) {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
        return AgendamentoMapper.toSaidaDto(entity);
    }

    public Page<AgendamentoSaidaDto> listarPorCliente(Long clienteId, Pageable pageable) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente não encontrado.");
        }
        return agendamentoRepository.findByClienteId(clienteId, pageable)
                .map(AgendamentoMapper::toSaidaDto);
    }

    public Page<AgendamentoSaidaDto> listarPorBarbeiro(Long barbeiroId, Pageable pageable) {
        if (!barbeiroRepository.existsById(barbeiroId)) {
            throw new ResourceNotFoundException("Barbeiro não encontrado.");
        }
        return agendamentoRepository.findByBarbeiroId(barbeiroId, pageable)
                .map(AgendamentoMapper::toSaidaDto);
    }

    public List<AgendamentoSaidaDto> listarPorBarbeiroEData(Long barbeiroId, LocalDate data) {
        if (!barbeiroRepository.existsById(barbeiroId)) {
            throw new ResourceNotFoundException("Barbeiro não encontrado.");
        }

        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        List<AgendamentoEntity> lista = agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioBetween(barbeiroId, inicioDoDia, fimDoDia);

        return AgendamentoMapper.toSaidaDtoList(lista);
    }

    public Page<AgendamentoSaidaDto> listarTodos(Pageable pageable) {
        return agendamentoRepository.findAll(pageable)
                .map(AgendamentoMapper::toSaidaDto);
    }


    // Retorna os horários livres de um barbeiro em uma data,
// considerando horário de funcionamento da barbearia + duração do serviço + bloqueios.
    public List<LocalTime> listarHorariosDisponiveis(Long barbeiroId, LocalDate data, int duracaoMinutos) {

        if (!barbeiroRepository.existsById(barbeiroId)) {
            throw new ResourceNotFoundException("Barbeiro não encontrado.");
        }

        // 1. Busca a barbearia principal (sistema de uma única barbearia)
        var barbearia = barbeariaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma barbearia cadastrada."));

        // 2. Converte o dia da data para o enum DiaSemana
        DiaSemana diaSemana = converterParaDiaSemana(data.getDayOfWeek());

        // 3. Busca os horários de funcionamento daquele dia
        List<HorarioFuncionamentoEntity> faixas =
                horarioFuncionamentoRepository.findByBarbeariaIdAndDiaSemana(
                        barbearia.getId(), diaSemana);

        // Se não tem horário cadastrado nesse dia → barbearia fechada
        if (faixas.isEmpty()) {
            return List.of();
        }

        // 4. Gera todos os possíveis horários de início (de 30 em 30 min) dentro das faixas
        List<LocalTime> todosHorarios = new ArrayList<>();

        for (HorarioFuncionamentoEntity faixa : faixas) {
            LocalTime abertura = faixa.getHorarioInicio();
            LocalTime fechamento = faixa.getHorarioFim();

            LocalTime horarioAtual = abertura;

            // Só adiciona se o slot inteiro (início + duração) cabe dentro da faixa
            while (!horarioAtual.plusMinutes(duracaoMinutos).isAfter(fechamento)) {
                todosHorarios.add(horarioAtual);
                horarioAtual = horarioAtual.plusMinutes(15);  // Gera todos os possíveis horários de início (de 15 em 15 min) dentro das faixas
            }
        }

        // 5. Busca agendamentos do dia (ignorando cancelados)
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        List<AgendamentoEntity> agendamentosDoDia = agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioBetweenAndStatusAgendamentoNotIn(
                        barbeiroId,
                        inicioDoDia,
                        fimDoDia,
                        List.of(StatusAgendamento.CANCELADO_CLIENTE,
                                StatusAgendamento.CANCELADO_BARBEIRO)
                );

        // 6. Filtra horários livres (sem conflito com agendamento e sem bloqueio)
        List<LocalTime> disponiveis = new ArrayList<>();

        for (LocalTime horario : todosHorarios) {
            LocalDateTime inicioSlot = data.atTime(horario);
            LocalDateTime fimSlot = inicioSlot.plusMinutes(duracaoMinutos);

            boolean temConflitoAgendamento = agendamentosDoDia.stream()
                    .anyMatch(a -> a.getDataHoraInicio().isBefore(fimSlot)
                            && a.getDataHoraFim().isAfter(inicioSlot));

            boolean temConflitoBloqueio = bloqueioService.existeBloqueioNoPeriodo(
                    barbeiroId, inicioSlot, fimSlot);

            if (!temConflitoAgendamento && !temConflitoBloqueio) {
                disponiveis.add(horario);
            }
        }

        return disponiveis;
    }

    // Método auxiliar privado
    private DiaSemana converterParaDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY    -> DiaSemana.SEGUNDA;
            case TUESDAY   -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY  -> DiaSemana.QUINTA;
            case FRIDAY    -> DiaSemana.SEXTA;
            case SATURDAY  -> DiaSemana.SABADO;
            case SUNDAY    -> DiaSemana.DOMINGO;
        };
    }


    // Atualizar status
    @Transactional
    public AgendamentoSaidaDto atualizarStatus(Long id, StatusAgendamento novoStatus) {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));

        entity.setStatusAgendamento(novoStatus);

        AgendamentoEntity atualizado = agendamentoRepository.save(entity);

        return AgendamentoMapper.toSaidaDto(atualizado);
    }


    //Cancelamento feito pelo Cliente. Só permite cancelar até 2 horas antes do horário.
    @Transactional
    public AgendamentoSaidaDto cancelarPorCliente(Long id, Long clienteId) throws BadRequestException {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));

        if (!entity.getCliente().getId().equals(clienteId)) {
            throw new BadRequestException("Você só pode cancelar os seus próprios agendamentos.");
        }

        if (entity.getStatusAgendamento() != StatusAgendamento.AGENDADO
                && entity.getStatusAgendamento() != StatusAgendamento.CONFIRMADO) {
            throw new BadRequestException("Este agendamento não pode mais ser cancelado.");
        }

        LocalDateTime limite = LocalDateTime.now().plusHours(2);
        if (entity.getDataHoraInicio().isBefore(limite)) {
            throw new BadRequestException("Não é possível cancelar com menos de 2 horas de antecedência.");
        }

        entity.setStatusAgendamento(StatusAgendamento.CANCELADO_CLIENTE);
        AgendamentoEntity atualizado = agendamentoRepository.save(entity);

        return AgendamentoMapper.toSaidaDto(atualizado);
    }


    // Cancelamento feito pelo Barbeiro.
    @Transactional
    public AgendamentoSaidaDto cancelarPorBarbeiro(Long id, Long barbeiroId) throws BadRequestException {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));

        if (!entity.getBarbeiro().getId().equals(barbeiroId)) {
            throw new BadRequestException("Você só pode cancelar agendamentos da sua agenda.");
        }

        if (entity.getStatusAgendamento() == StatusAgendamento.CONCLUIDO
                || entity.getStatusAgendamento() == StatusAgendamento.CANCELADO_CLIENTE
                || entity.getStatusAgendamento() == StatusAgendamento.CANCELADO_BARBEIRO) {
            throw new BadRequestException("Este agendamento não pode mais ser cancelado.");
        }

        entity.setStatusAgendamento(StatusAgendamento.CANCELADO_BARBEIRO);
        AgendamentoEntity atualizado = agendamentoRepository.save(entity);

        return AgendamentoMapper.toSaidaDto(atualizado);
    }


    // REAGENDAR
    @Transactional
    public AgendamentoSaidaDto reagendar(Long id, LocalDateTime novaDataHoraInicio) throws BadRequestException {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));

        if (entity.getStatusAgendamento() != StatusAgendamento.AGENDADO
                && entity.getStatusAgendamento() != StatusAgendamento.CONFIRMADO) {
            throw new BadRequestException("Só é possível reagendar agendamentos com status AGENDADO ou CONFIRMADO.");
        }

        int duracao = entity.getServico().getDuracaoEmMinutos();
        LocalDateTime novaDataHoraFim = novaDataHoraInicio.plusMinutes(duracao);

        if (existeConflito(entity.getBarbeiro().getId(), novaDataHoraInicio, novaDataHoraFim, entity.getId())) {
            throw new BadRequestException("Já existe um agendamento ou bloqueio neste novo horário.");
        }

        entity.setDataHoraInicio(novaDataHoraInicio);
        entity.setDataHoraFim(novaDataHoraFim);

        AgendamentoEntity atualizado = agendamentoRepository.save(entity);
        return AgendamentoMapper.toSaidaDto(atualizado);
    }

    // FLUXO DE ATENDIMENTO
    @Transactional
    public AgendamentoSaidaDto iniciarAtendimento(Long id) {
        return atualizarStatus(id, StatusAgendamento.EM_ANDAMENTO);
    }

    @Transactional
    public AgendamentoSaidaDto finalizarAtendimento(Long id) {
        return atualizarStatus(id, StatusAgendamento.CONCLUIDO);
    }

    @Transactional
    public AgendamentoSaidaDto marcarNaoCompareceu(Long id) {
        return atualizarStatus(id, StatusAgendamento.NAO_COMPARECEU);
    }




}
