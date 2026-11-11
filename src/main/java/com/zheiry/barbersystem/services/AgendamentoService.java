package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.agendamento.AgendamentoDto;
import com.zheiry.barbersystem.dto.agendamento.AgendamentoSaidaDto;
import com.zheiry.barbersystem.dto.agendamento.FecharComandaDto;
import com.zheiry.barbersystem.dto.agendamento.HistoricoAgendamentoDto;
import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.entities.*;
import com.zheiry.barbersystem.enums.DiaSemana;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.enums.TipoHistoricoAgendamento;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.AgendamentoMapper;
import com.zheiry.barbersystem.mappers.HistoricoAgendamentoMapper;
import com.zheiry.barbersystem.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.Clock;
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
    private final ComissaoService comissaoService;
    private final NotificacaoService notificacaoService;
    private final Clock relogio;
    private final HistoricoAgendamentoRepository historicoAgendamentoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository, BarbeiroRepository barbeiroRepository, ServicoRepository servicoRepository, HorarioFuncionamentoRepository horarioFuncionamentoRepository, BarbeariaRepository barbeariaRepository, BloqueioService bloqueioService, ComissaoService comissaoService, NotificacaoService notificacaoService, Clock relogio) {
        this(agendamentoRepository, clienteRepository, barbeiroRepository, servicoRepository,
                horarioFuncionamentoRepository, barbeariaRepository, bloqueioService, comissaoService,
                notificacaoService, relogio, null);
    }

    @Autowired
    public AgendamentoService(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository, BarbeiroRepository barbeiroRepository, ServicoRepository servicoRepository, HorarioFuncionamentoRepository horarioFuncionamentoRepository, BarbeariaRepository barbeariaRepository, BloqueioService bloqueioService, ComissaoService comissaoService, NotificacaoService notificacaoService, Clock relogio, HistoricoAgendamentoRepository historicoAgendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.servicoRepository = servicoRepository;
        this.horarioFuncionamentoRepository = horarioFuncionamentoRepository;
        this.barbeariaRepository = barbeariaRepository;
        this.bloqueioService = bloqueioService;
        this.comissaoService = comissaoService;
        this.notificacaoService = notificacaoService;
        this.relogio = relogio;
        this.historicoAgendamentoRepository = historicoAgendamentoRepository;
    }

    // Verifica se existe conflito de horário para o barbeiro.
    //Busca os agendamentos do dia e verifica a sobreposição em memória.
    public boolean existeConflito(Long barbeiroId, LocalDateTime inicio, LocalDateTime fim, Long agendamentoIdExcluido) {

        // Verifica se existe bloqueio no periodo
        if (bloqueioService.existeBloqueioNoPeriodo(barbeiroId, inicio, fim)){
            return true;
        }

        // Verifica conflito com outros agendamentos
        List<AgendamentoEntity> agendamentos = agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioLessThanAndDataHoraFimGreaterThan(barbeiroId, fim, inicio);

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

        Authentication authentication = autenticacaoAtual();
        Long usuarioId = idAutenticado(authentication);
        if (temRole(authentication, "CLIENTE") && (dto.clienteId() == null || !usuarioId.equals(dto.clienteId()))) {
            throw new AccessDeniedException("Clientes só podem criar agendamentos para si mesmos.");
        }
        if (temRole(authentication, "BARBEIRO") && !usuarioId.equals(dto.barbeiroId())) {
            throw new AccessDeniedException("Barbeiros só podem criar agendamentos para sua própria agenda.");
        }
        if (!temRole(authentication, "CLIENTE") && !temRole(authentication, "BARBEIRO")
                && !temRole(authentication, "ADMINISTRADOR")) {
            throw new AccessDeniedException("Perfil sem permissão para criar agendamentos.");
        }

        boolean clienteAvulso = dto.clienteId() == null;
        if (clienteAvulso && (temRole(authentication, "CLIENTE") || dto.clienteAvulsoNome() == null || dto.clienteAvulsoNome().isBlank())) {
            throw new BadRequestException("Informe o nome do cliente ou selecione um cliente cadastrado.");
        }
        if (clienteAvulso && dto.clienteAvulsoNome().trim().length() > 255) {
            throw new BadRequestException("O nome do cliente não pode ultrapassar 255 caracteres.");
        }
        if (clienteAvulso && dto.clienteAvulsoTelefone() != null && dto.clienteAvulsoTelefone().length() > 255) {
            throw new BadRequestException("O telefone do cliente não pode ultrapassar 255 caracteres.");
        }
        if (!clienteAvulso && dto.clienteAvulsoNome() != null && !dto.clienteAvulsoNome().isBlank()) {
            throw new BadRequestException("Selecione um cliente cadastrado ou informe os dados do cliente de porta.");
        }

        ClienteEntity cliente = clienteAvulso ? null : clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        if (cliente != null && cliente.getStatus() != Status.ATIVO) {
            throw new BadRequestException("O cliente está inativo e não pode realizar agendamentos.");
        }

        // Todas as criações e reagendamentos bloqueiam a mesma linha do barbeiro antes
        // de verificar conflitos, serializando reservas concorrentes para essa agenda.
        BarbeiroEntity barbeiro = barbeiroRepository.findByIdForUpdate(dto.barbeiroId())
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
        if (dataHoraInicio == null || !dataHoraInicio.isAfter(LocalDateTime.now(relogio))) {
            throw new BadRequestException("O agendamento deve começar no futuro.");
        }
        LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(servico.getDuracaoEmMinutos());

        validarHorarioFuncionamento(dataHoraInicio, dataHoraFim);

        // Verifica conflito de horário
        if (existeConflito(barbeiro.getId(), dataHoraInicio, dataHoraFim, null)) {
            throw new BadRequestException("Já existe um agendamento ou bloqueio para este barbeiro neste horário.");
        }

        AgendamentoEntity entity = AgendamentoMapper.toEntity(dto, cliente, barbeiro, servico);
        AgendamentoEntity salvo = agendamentoRepository.save(entity);
        notificacaoService.agendamentoCriado(salvo);

        return AgendamentoMapper.toSaidaDto(salvo);
    }


    // Buscar agendamento por ID
    public AgendamentoSaidaDto buscarPorId(Long id) {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
        validarAcessoAoAgendamento(entity);
        return AgendamentoMapper.toSaidaDto(entity);
    }

    public List<HistoricoAgendamentoDto> listarHistorico(Long agendamentoId) {
        AgendamentoEntity agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
        validarAcessoAoAgendamento(agendamento);
        return HistoricoAgendamentoMapper.paraSaidaDtoList(
                historicoAgendamentoRepository.findByAgendamentoIdOrderByRegistradoEmAsc(agendamentoId));
    }

    private void registrarHistorico(AgendamentoEntity agendamento, TipoHistoricoAgendamento tipoEvento,
                                    LocalDateTime dataHoraAnterior, LocalDateTime dataHoraNova,
                                    ServicoEntity servicoAdicional) {
        if (historicoAgendamentoRepository == null) return;
        Authentication authentication = autenticacaoAtual();
        UsuarioAutenticado usuario = authentication.getPrincipal() instanceof UsuarioAutenticado autenticado
                ? autenticado : null;

        HistoricoAgendamentoEntity historico = new HistoricoAgendamentoEntity();
        historico.setAgendamentoId(agendamento.getId());
        historico.setTipoEvento(tipoEvento);
        historico.setRegistradoEm(LocalDateTime.now(relogio));
        historico.setUsuarioId(usuario != null ? usuario.getId() : null);
        historico.setUsuarioEmail(authentication.getName());
        historico.setPerfilUsuario(authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring(5))
                .findFirst().orElse(null));
        historico.setClienteId(agendamento.getCliente() != null ? agendamento.getCliente().getId() : null);
        historico.setClienteNome(agendamento.getCliente() != null
                ? agendamento.getCliente().getNome() : agendamento.getClienteAvulsoNome());
        historico.setBarbeiroId(agendamento.getBarbeiro().getId());
        historico.setBarbeiroNome(agendamento.getBarbeiro().getNome());
        historico.setServicoId(agendamento.getServico().getId());
        historico.setServicoNome(agendamento.getServico().getNome());
        historico.setValorServico(java.math.BigDecimal.valueOf(agendamento.getServico().getPreco())
                .setScale(2, java.math.RoundingMode.HALF_UP));
        if (servicoAdicional != null) {
            historico.setServicoAdicionalId(servicoAdicional.getId());
            historico.setServicoAdicionalNome(servicoAdicional.getNome());
            historico.setValorServicoAdicional(java.math.BigDecimal.valueOf(servicoAdicional.getPreco())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
        }
        historico.setDataHoraAnterior(dataHoraAnterior);
        historico.setDataHoraNova(dataHoraNova != null ? dataHoraNova : agendamento.getDataHoraInicio());
        historico.setFormaPagamento(agendamento.getFormaPagamento());
        historico.setValorTotalComanda(agendamento.getValorTotalComanda());
        historicoAgendamentoRepository.save(historico);
    }

    public Page<AgendamentoSaidaDto> listarPorCliente(Long clienteId, Pageable pageable) {
        Authentication authentication = autenticacaoAtual();
        if (!temRole(authentication, "ADMINISTRADOR")
                && (!temRole(authentication, "CLIENTE") || !idAutenticado(authentication).equals(clienteId))) {
            throw new AccessDeniedException("Você só pode consultar seus próprios agendamentos.");
        }
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente não encontrado.");
        }
        return agendamentoRepository.findByClienteId(clienteId, pageable)
                .map(AgendamentoMapper::toSaidaDto);
    }

    public List<AgendamentoSaidaDto> listarPorClienteEPeriodo(Long clienteId, LocalDate inicio, LocalDate fim) {
        Authentication authentication = autenticacaoAtual();
        if (!temRole(authentication, "ADMINISTRADOR")
                && (!temRole(authentication, "CLIENTE") || !idAutenticado(authentication).equals(clienteId))) {
            throw new AccessDeniedException("Você só pode consultar seus próprios agendamentos.");
        }
        validarPeriodo(inicio, fim);
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente não encontrado.");
        }
        return AgendamentoMapper.toSaidaDtoList(agendamentoRepository
                .findByClienteIdAndDataHoraInicioGreaterThanEqualAndDataHoraInicioLessThanOrderByDataHoraInicioAsc(
                        clienteId, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()));
    }

    public Page<AgendamentoSaidaDto> listarPorBarbeiro(Long barbeiroId, Pageable pageable) {
        Authentication authentication = autenticacaoAtual();
        if (!temRole(authentication, "ADMINISTRADOR")
                && (!temRole(authentication, "BARBEIRO") || !idAutenticado(authentication).equals(barbeiroId))) {
            throw new AccessDeniedException("Você só pode consultar a sua própria agenda.");
        }
        if (!barbeiroRepository.existsById(barbeiroId)) {
            throw new ResourceNotFoundException("Barbeiro não encontrado.");
        }
        return agendamentoRepository.findByBarbeiroId(barbeiroId, pageable)
                .map(AgendamentoMapper::toSaidaDto);
    }

    public List<AgendamentoSaidaDto> listarPorBarbeiroEData(Long barbeiroId, LocalDate data) {
        Authentication authentication = autenticacaoAtual();
        if (!temRole(authentication, "ADMINISTRADOR")
                && (!temRole(authentication, "BARBEIRO") || !idAutenticado(authentication).equals(barbeiroId))) {
            throw new AccessDeniedException("Você só pode consultar a sua própria agenda.");
        }
        if (!barbeiroRepository.existsById(barbeiroId)) {
            throw new ResourceNotFoundException("Barbeiro não encontrado.");
        }

        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        List<AgendamentoEntity> lista = agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioBetween(barbeiroId, inicioDoDia, fimDoDia);

        return AgendamentoMapper.toSaidaDtoList(lista);
    }

    public List<AgendamentoSaidaDto> listarPorBarbeiroEPeriodo(Long barbeiroId, LocalDate inicio, LocalDate fim) {
        Authentication authentication = autenticacaoAtual();
        if (!temRole(authentication, "ADMINISTRADOR")
                && (!temRole(authentication, "BARBEIRO") || !idAutenticado(authentication).equals(barbeiroId))) {
            throw new AccessDeniedException("Você só pode consultar a sua própria agenda.");
        }
        validarPeriodo(inicio, fim);
        if (!barbeiroRepository.existsById(barbeiroId)) {
            throw new ResourceNotFoundException("Barbeiro não encontrado.");
        }
        return AgendamentoMapper.toSaidaDtoList(agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioGreaterThanEqualAndDataHoraInicioLessThanOrderByDataHoraInicioAsc(
                        barbeiroId, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()));
    }

    public List<AgendamentoSaidaDto> listarTodosNoPeriodo(LocalDate inicio, LocalDate fim) {
        if (!temRole(autenticacaoAtual(), "ADMINISTRADOR")) {
            throw new AccessDeniedException("Somente administradores podem consultar todos os agendamentos.");
        }
        validarPeriodo(inicio, fim);
        return AgendamentoMapper.toSaidaDtoList(agendamentoRepository
                .findByDataHoraInicioGreaterThanEqualAndDataHoraInicioLessThanOrderByDataHoraInicioAsc(
                        inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()));
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new BadRequestException("Informe um período válido, com data final igual ou posterior à inicial.");
        }
        if (fim.isAfter(inicio.plusMonths(12))) {
            throw new BadRequestException("O período consultado não pode ultrapassar 12 meses.");
        }
    }

    public Page<AgendamentoSaidaDto> listarTodos(Pageable pageable) {
        if (!temRole(autenticacaoAtual(), "ADMINISTRADOR")) {
            throw new AccessDeniedException("Somente administradores podem consultar todos os agendamentos.");
        }
        return agendamentoRepository.findAll(pageable)
                .map(AgendamentoMapper::toSaidaDto);
    }


    // Retorna os horários livres de um barbeiro em uma data,
// considerando horário de funcionamento da barbearia + duração do serviço + bloqueios.
    public List<LocalTime> listarHorariosDisponiveis(Long barbeiroId, LocalDate data, int duracaoMinutos) {

        if (duracaoMinutos <= 0) {
            throw new BadRequestException("A duração deve ser maior que zero.");
        }

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
                .findByBarbeiroIdAndDataHoraInicioLessThanAndDataHoraFimGreaterThanAndStatusAgendamentoNotIn(
                        barbeiroId,
                        fimDoDia,
                        inicioDoDia,
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
        AgendamentoEntity entity = agendamentoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));

        if (novoStatus == StatusAgendamento.CONCLUIDO) {
            throw new BadRequestException("Para concluir o atendimento, feche a comanda com a forma de pagamento.");
        }
        validarResponsavelOuAdministrador(entity);
        validarTransicaoStatus(entity, novoStatus);

        entity.setStatusAgendamento(novoStatus);

        AgendamentoEntity atualizado = agendamentoRepository.save(entity);

        if (novoStatus == StatusAgendamento.NAO_COMPARECEU) {
            comissaoService.removerConclusao(atualizado.getId());
            registrarHistorico(atualizado, TipoHistoricoAgendamento.NAO_COMPARECEU, null, null, null);
        } else if (novoStatus == StatusAgendamento.CANCELADO_BARBEIRO) {
            registrarHistorico(atualizado, TipoHistoricoAgendamento.CANCELADO, null, null, null);
        }

        if (novoStatus == StatusAgendamento.CONFIRMADO) {
            notificacaoService.agendamentoConfirmado(atualizado);
        }

        return AgendamentoMapper.toSaidaDto(atualizado);
    }


    //Cancelamento feito pelo Cliente. Só permite cancelar até 2 horas antes do horário.
    @Transactional
    public AgendamentoSaidaDto cancelarPorCliente(Long id) throws BadRequestException {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));

        validarDono(entity, "CLIENTE");

        if (entity.getStatusAgendamento() != StatusAgendamento.AGENDADO
                && entity.getStatusAgendamento() != StatusAgendamento.CONFIRMADO) {
            throw new BadRequestException("Este agendamento não pode mais ser cancelado.");
        }

        LocalDateTime limite = LocalDateTime.now(relogio).plusHours(2);
        if (entity.getDataHoraInicio().isBefore(limite)) {
            throw new BadRequestException("Não é possível cancelar com menos de 2 horas de antecedência.");
        }

        entity.setStatusAgendamento(StatusAgendamento.CANCELADO_CLIENTE);
        AgendamentoEntity atualizado = agendamentoRepository.save(entity);
        registrarHistorico(atualizado, TipoHistoricoAgendamento.CANCELADO, null, null, null);
        notificacaoService.agendamentoCancelado(atualizado);

        return AgendamentoMapper.toSaidaDto(atualizado);
    }


    // Cancelamento feito pelo Barbeiro.
    @Transactional
    public AgendamentoSaidaDto cancelarPorBarbeiro(Long id) throws BadRequestException {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));

        validarResponsavelOuAdministrador(entity);

        if (entity.getStatusAgendamento() != StatusAgendamento.AGENDADO
                && entity.getStatusAgendamento() != StatusAgendamento.CONFIRMADO
                && entity.getStatusAgendamento() != StatusAgendamento.EM_ANDAMENTO) {
            throw new BadRequestException("Este agendamento não pode mais ser cancelado.");
        }

        entity.setStatusAgendamento(StatusAgendamento.CANCELADO_BARBEIRO);
        AgendamentoEntity atualizado = agendamentoRepository.save(entity);
        registrarHistorico(atualizado, TipoHistoricoAgendamento.CANCELADO, null, null, null);
        notificacaoService.agendamentoCancelado(atualizado);

        return AgendamentoMapper.toSaidaDto(atualizado);
    }


    // REAGENDAR
    @Transactional
    public AgendamentoSaidaDto reagendar(Long id, LocalDateTime novaDataHoraInicio) throws BadRequestException {
        AgendamentoEntity entity = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));

        validarDonoOuResponsavelOuAdministrador(entity);

        barbeiroRepository.findByIdForUpdate(entity.getBarbeiro().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado."));

        if (entity.getStatusAgendamento() != StatusAgendamento.AGENDADO
                && entity.getStatusAgendamento() != StatusAgendamento.CONFIRMADO) {
            throw new BadRequestException("Só é possível reagendar agendamentos com status AGENDADO ou CONFIRMADO.");
        }

        int duracao = entity.getServico().getDuracaoEmMinutos();
        if (novaDataHoraInicio == null || !novaDataHoraInicio.isAfter(LocalDateTime.now(relogio))) {
            throw new BadRequestException("O novo horário deve começar no futuro.");
        }
        LocalDateTime novaDataHoraFim = novaDataHoraInicio.plusMinutes(duracao);

        validarHorarioFuncionamento(novaDataHoraInicio, novaDataHoraFim);

        if (existeConflito(entity.getBarbeiro().getId(), novaDataHoraInicio, novaDataHoraFim, entity.getId())) {
            throw new BadRequestException("Já existe um agendamento ou bloqueio neste novo horário.");
        }

        LocalDateTime dataHoraAnterior = entity.getDataHoraInicio();
        entity.setDataHoraInicio(novaDataHoraInicio);
        entity.setDataHoraFim(novaDataHoraFim);

        AgendamentoEntity atualizado = agendamentoRepository.save(entity);
        registrarHistorico(atualizado, TipoHistoricoAgendamento.REAGENDADO,
                dataHoraAnterior, novaDataHoraInicio, null);
        notificacaoService.agendamentoReagendado(atualizado);
        return AgendamentoMapper.toSaidaDto(atualizado);
    }

    // FLUXO DE ATENDIMENTO
    @Transactional
    public AgendamentoSaidaDto iniciarAtendimento(Long id) {
        return atualizarStatus(id, StatusAgendamento.EM_ANDAMENTO);
    }

    @Transactional
    public AgendamentoSaidaDto fecharComanda(Long id, FecharComandaDto dto) {
        AgendamentoEntity agendamento = agendamentoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
        validarResponsavelOuAdministrador(agendamento);
        if (agendamento.getStatusAgendamento() != StatusAgendamento.AGENDADO
                && agendamento.getStatusAgendamento() != StatusAgendamento.CONFIRMADO
                && agendamento.getStatusAgendamento() != StatusAgendamento.EM_ANDAMENTO) {
            throw new BadRequestException("A comanda só pode ser fechada para um atendimento ativo.");
        }
        if (dto == null || dto.formaPagamento() == null) {
            throw new BadRequestException("A forma de pagamento é obrigatória.");
        }

        ServicoEntity servicoAdicional = null;
        if (dto.servicoAdicionalId() != null) {
            if (dto.servicoAdicionalId().equals(agendamento.getServico().getId())) {
                throw new BadRequestException("O serviço adicional já está na comanda.");
            }
            servicoAdicional = servicoRepository.findById(dto.servicoAdicionalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço adicional não encontrado."));
            if (servicoAdicional.getStatus() != Status.ATIVO) {
                throw new BadRequestException("O serviço adicional está inativo.");
            }
        }

        java.math.BigDecimal total = java.math.BigDecimal.valueOf(agendamento.getServico().getPreco())
                .setScale(2, java.math.RoundingMode.HALF_UP);
        if (servicoAdicional != null) {
            total = total.add(java.math.BigDecimal.valueOf(servicoAdicional.getPreco())
                    .setScale(2, java.math.RoundingMode.HALF_UP));
        }
        agendamento.setFormaPagamento(dto.formaPagamento());
        agendamento.setValorTotalComanda(total);
        agendamento.setStatusAgendamento(StatusAgendamento.CONCLUIDO);
        AgendamentoEntity atualizado = agendamentoRepository.save(agendamento);
        comissaoService.registrarConclusao(atualizado, servicoAdicional);
        registrarHistorico(atualizado, TipoHistoricoAgendamento.CONCLUIDO, null, null, servicoAdicional);
        return AgendamentoMapper.toSaidaDto(atualizado);
    }

    @Transactional
    public AgendamentoSaidaDto marcarNaoCompareceu(Long id) {
        return atualizarStatus(id, StatusAgendamento.NAO_COMPARECEU);
    }

    private void validarTransicaoStatus(AgendamentoEntity entity, StatusAgendamento novo) {
        if (novo == null) {
            throw new BadRequestException("O novo status é obrigatório.");
        }

        StatusAgendamento atual = entity.getStatusAgendamento();
        boolean expirado = !entity.getDataHoraFim().isAfter(LocalDateTime.now(relogio));
        boolean permitido = switch (atual) {
            case AGENDADO -> novo == StatusAgendamento.CONFIRMADO
                    || novo == StatusAgendamento.CANCELADO_BARBEIRO
                    || (expirado && novo == StatusAgendamento.NAO_COMPARECEU);
            case CONFIRMADO -> novo == StatusAgendamento.CANCELADO_BARBEIRO
                    || (expirado && novo == StatusAgendamento.NAO_COMPARECEU);
            case EM_ANDAMENTO -> novo == StatusAgendamento.CANCELADO_BARBEIRO
                    || (expirado && novo == StatusAgendamento.NAO_COMPARECEU);
            case CONCLUIDO, CANCELADO_CLIENTE, CANCELADO_BARBEIRO, NAO_COMPARECEU -> false;
        };

        if (!permitido) {
            throw new BadRequestException("Transição de status inválida: " + atual + " para " + novo + ".");
        }
    }

    private void validarResponsavelOuAdministrador(AgendamentoEntity entity) {
        Authentication authentication = autenticacaoAtual();
        if (temRole(authentication, "ADMINISTRADOR")) {
            return;
        }
        if (!temRole(authentication, "BARBEIRO") || !idAutenticado(authentication).equals(entity.getBarbeiro().getId())) {
            throw new AccessDeniedException("Você só pode atuar nos agendamentos da sua agenda.");
        }
    }

    private void validarDono(AgendamentoEntity entity, String roleEsperada) {
        Authentication authentication = autenticacaoAtual();
        if (entity.getCliente() == null || !temRole(authentication, roleEsperada) || !idAutenticado(authentication).equals(entity.getCliente().getId())) {
            throw new AccessDeniedException("Você só pode atuar nos seus próprios agendamentos.");
        }
    }

    private void validarDonoOuResponsavelOuAdministrador(AgendamentoEntity entity) {
        Authentication authentication = autenticacaoAtual();
        if (temRole(authentication, "ADMINISTRADOR")) {
            return;
        }
        Long id = idAutenticado(authentication);
        if (temRole(authentication, "CLIENTE") && entity.getCliente() != null && id.equals(entity.getCliente().getId())) {
            return;
        }
        if (temRole(authentication, "BARBEIRO") && id.equals(entity.getBarbeiro().getId())) {
            return;
        }
        throw new AccessDeniedException("Você não pode reagendar este agendamento.");
    }

    private void validarAcessoAoAgendamento(AgendamentoEntity entity) {
        Authentication authentication = autenticacaoAtual();
        if (temRole(authentication, "ADMINISTRADOR")) return;
        Long id = idAutenticado(authentication);
        if (temRole(authentication, "CLIENTE") && entity.getCliente() != null && id.equals(entity.getCliente().getId())) return;
        if (temRole(authentication, "BARBEIRO") && id.equals(entity.getBarbeiro().getId())) return;
        throw new AccessDeniedException("Você não pode consultar este agendamento.");
    }

    private void validarHorarioFuncionamento(LocalDateTime inicio, LocalDateTime fim) {
        var barbearia = barbeariaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma barbearia cadastrada."));
        DiaSemana diaSemana = converterParaDiaSemana(inicio.getDayOfWeek());
        boolean dentroDoHorario = horarioFuncionamentoRepository
                .findByBarbeariaIdAndDiaSemana(barbearia.getId(), diaSemana).stream()
                .anyMatch(faixa -> !inicio.toLocalTime().isBefore(faixa.getHorarioInicio())
                        && !fim.toLocalTime().isAfter(faixa.getHorarioFim())
                        && inicio.toLocalDate().equals(fim.toLocalDate()));
        if (!dentroDoHorario) {
            throw new BadRequestException("O horário solicitado está fora do funcionamento da barbearia.");
        }
    }

    private Authentication autenticacaoAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Autenticação obrigatória.");
        }
        return authentication;
    }

    private boolean temRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    private Long idAutenticado(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario.getId();
        }
        throw new AccessDeniedException("Não foi possível identificar o usuário autenticado.");
    }




}
