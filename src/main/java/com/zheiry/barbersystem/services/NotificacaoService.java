package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.NotificacaoConfiguracao;
import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.dto.notificacao.EstadoIntegracoesNotificacaoDto;
import com.zheiry.barbersystem.dto.notificacao.NotificacaoSaidaDto;
import com.zheiry.barbersystem.dto.notificacao.PreferenciaNotificacaoAtualizaDto;
import com.zheiry.barbersystem.dto.notificacao.PreferenciaNotificacaoSaidaDto;
import com.zheiry.barbersystem.dto.notificacao.ResultadoEnvio;
import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.CanalNotificacao;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.enums.StatusNotificacao;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.enums.TipoNotificacao;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.NotificacaoMapper;
import com.zheiry.barbersystem.repositories.AgendamentoRepository;
import com.zheiry.barbersystem.repositories.BarbeariaRepository;
import com.zheiry.barbersystem.repositories.ClienteRepository;
import com.zheiry.barbersystem.repositories.NotificacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class NotificacaoService {
    private static final List<TipoNotificacao> TIPOS_LEMBRETE = List.of(TipoNotificacao.LEMBRETE_2H);
    private static final String PREFERENCIA_DESATIVADA = "Preferência de notificações desativada pelo cliente.";
    private final NotificacaoRepository notificacaoRepository;
    private final ClienteRepository clienteRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final NotificacaoConfiguracao configuracao;
    private final Clock relogio;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, ClienteRepository clienteRepository,
                              BarbeariaRepository barbeariaRepository, AgendamentoRepository agendamentoRepository,
                              NotificacaoConfiguracao configuracao, Clock relogio) {
        this.notificacaoRepository = notificacaoRepository;
        this.clienteRepository = clienteRepository;
        this.barbeariaRepository = barbeariaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.configuracao = configuracao;
        this.relogio = relogio;
    }

    @Transactional
    public PreferenciaNotificacaoSaidaDto consultarPreferencia() {
        return NotificacaoMapper.paraPreferencia(clienteAutenticado());
    }

    @Transactional
    public PreferenciaNotificacaoSaidaDto atualizarPreferencia(PreferenciaNotificacaoAtualizaDto dto) {
        ClienteEntity cliente = clienteAutenticado();
        validarContato(cliente, dto.canal());
        NotificacaoMapper.atualizarPreferencia(dto, cliente);
        clienteRepository.save(cliente);

        String destinatario = obterDestinatario(cliente, dto.canal());
        for (NotificacaoEntity notificacao : notificacaoRepository.findByClienteIdAndStatus(cliente.getId(), StatusNotificacao.PENDENTE)) {
            if (!dto.autorizada()) {
                notificacao.setStatus(StatusNotificacao.CANCELADA);
                notificacao.setErroSeguro(PREFERENCIA_DESATIVADA);
            } else {
                notificacao.setCanal(CanalNotificacao.EMAIL.name());
                notificacao.setDestinatario(destinatario);
            }
            notificacaoRepository.save(notificacao);
        }

        if (dto.autorizada()) {
            LocalDateTime agora = agora();
            List<AgendamentoEntity> futuros = agendamentoRepository.findByClienteIdAndStatusAgendamentoIn(
                    cliente.getId(), List.of(StatusAgendamento.AGENDADO, StatusAgendamento.CONFIRMADO));
            for (AgendamentoEntity agendamento : futuros) {
                if (agendamento.getDataHoraInicio().isAfter(agora)) programarLembretes(agendamento);
            }
        }
        return NotificacaoMapper.paraPreferencia(cliente);
    }

    @Transactional
    public List<NotificacaoSaidaDto> historicoDoCliente() {
        ClienteEntity cliente = clienteAutenticado();
        return NotificacaoMapper.paraSaida(notificacaoRepository
                .findByClienteIdOrderByCriadaEmDesc(cliente.getId(), PageRequest.of(0, 100)));
    }

    public EstadoIntegracoesNotificacaoDto estadoIntegracoes() {
        return new EstadoIntegracoesNotificacaoDto(configuracao.estadoResend());
    }

    @Transactional
    public void agendamentoCriado(AgendamentoEntity agendamento) {
        criarNotificacao(agendamento, TipoNotificacao.CONFIRMACAO, agora(), agendamento.getDataHoraInicio());
        programarLembretes(agendamento);
    }

    @Transactional
    public void agendamentoConfirmado(AgendamentoEntity agendamento) {
        criarNotificacao(agendamento, TipoNotificacao.CONFIRMACAO, agora(), agendamento.getDataHoraInicio());
        programarLembretes(agendamento);
    }

    @Transactional
    public void agendamentoCancelado(AgendamentoEntity agendamento) {
        cancelarLembretes(agendamento.getId(), "Agendamento cancelado.");
        criarNotificacao(agendamento, TipoNotificacao.CANCELAMENTO, agora(), agendamento.getDataHoraInicio());
    }

    @Transactional
    public void agendamentoReagendado(AgendamentoEntity agendamento) {
        cancelarLembretes(agendamento.getId(), "Horário alterado; lembrete antigo invalidado.");
        criarNotificacao(agendamento, TipoNotificacao.CONFIRMACAO, agora(), agendamento.getDataHoraInicio());
        programarLembretes(agendamento);
    }

    @Transactional
    public void revisarLembretesFuturos() {
        LocalDateTime agora = agora();
        for (AgendamentoEntity agendamento : agendamentoRepository.findByDataHoraInicioAfterAndStatusAgendamentoIn(
                agora, List.of(StatusAgendamento.AGENDADO, StatusAgendamento.CONFIRMADO))) {
            programarLembretes(agendamento);
        }
    }

    @Transactional
    public void programarAniversariantes() {
        LocalDate hoje = LocalDate.now(relogio);
        LocalDateTime envio = hoje.atTime(9, 0);
        for (ClienteEntity cliente : clienteRepository.buscarAniversariantes(Status.ATIVO, hoje.getMonthValue(), hoje.getDayOfMonth())) {
            if (cliente.getEmail() == null || cliente.getEmail().isBlank()) continue;
            String chave = "cliente:" + cliente.getId() + ":ANIVERSARIO:" + hoje.getYear();
            if (notificacaoRepository.findByChaveIdempotencia(chave).isPresent()) continue;
            NotificacaoEntity notificacao = new NotificacaoEntity();
            notificacao.setClienteId(cliente.getId());
            notificacao.setTipo(TipoNotificacao.ANIVERSARIO);
            notificacao.setCanal(CanalNotificacao.EMAIL.name());
            notificacao.setChaveIdempotencia(chave);
            notificacao.setDestinatario(cliente.getEmail());
            notificacao.setNomeCliente(cliente.getNome());
            notificacao.setNomeBarbearia(barbeariaRepository.findFirstByOrderByIdAsc().map(b -> b.getNomeFantasia()).orElse("Barbearia"));
            notificacao.setDataHoraAgendamento(envio);
            notificacao.setProgramadaPara(envio.isAfter(agora()) ? envio : agora());
            notificacao.setCriadaEm(agora());
            notificacaoRepository.save(notificacao);
        }
    }

    @Transactional
    public List<Long> buscarNotificacoesDevidas(LocalDateTime agora) {
        return notificacaoRepository.buscarIdsDevidas(StatusNotificacao.PENDENTE, StatusNotificacao.PROCESSANDO,
                agora, agora.minusMinutes(configuracao.getLeaseMinutos()), PageRequest.of(0, 100));
    }

    @Transactional
    public NotificacaoEntity reservar(Long id, LocalDateTime agora) {
        int reservada = notificacaoRepository.reservar(id, StatusNotificacao.PENDENTE,
                StatusNotificacao.PROCESSANDO, agora, agora.minusMinutes(configuracao.getLeaseMinutos()));
        if (reservada == 0) return null;
        return notificacaoRepository.findById(id).orElse(null);
    }

    @Transactional
    public void registrarEnvio(Long id, ResultadoEnvio resultado, LocalDateTime agora) {
        NotificacaoEntity notificacao = notificacaoRepository.findById(id).orElseThrow();
        notificacao.setStatus(StatusNotificacao.ENVIADA);
        notificacao.setEnviadaEm(agora);
        notificacao.setCodigoProvedor(resultado.identificadorProvedor());
        notificacao.setErroSeguro(null);
        notificacao.setProximaTentativaEm(null);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void liberarReserva(Long id, LocalDateTime agora) {
        NotificacaoEntity notificacao = notificacaoRepository.findById(id).orElseThrow();
        if (notificacao.getStatus() == StatusNotificacao.PROCESSANDO) {
            notificacao.setStatus(StatusNotificacao.PENDENTE);
            notificacao.setTentadaEm(null);
            notificacao.setQuantidadeTentativas(Math.max(0, notificacao.getQuantidadeTentativas() - 1));
            notificacaoRepository.save(notificacao);
        }
    }

    @Transactional
    public void cancelarCanalDesativado(Long id) {
        NotificacaoEntity notificacao = notificacaoRepository.findById(id).orElseThrow();
        notificacao.setStatus(StatusNotificacao.CANCELADA);
        notificacao.setErroSeguro("Canal de notificação desativado.");
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void registrarFalha(Long id, String mensagemSegura, boolean retentavel, LocalDateTime agora) {
        NotificacaoEntity notificacao = notificacaoRepository.findById(id).orElseThrow();
        notificacao.setErroSeguro(mensagemSegura.length() > 500 ? mensagemSegura.substring(0, 500) : mensagemSegura);
        if (!retentavel || notificacao.getQuantidadeTentativas() >= configuracao.getMaxTentativas()) {
            notificacao.setStatus(StatusNotificacao.FALHA);
            notificacao.setProximaTentativaEm(null);
        } else {
            notificacao.setStatus(StatusNotificacao.PENDENTE);
            int minutos = Math.min(360, 1 << Math.min(notificacao.getQuantidadeTentativas(), 8));
            notificacao.setProximaTentativaEm(agora.plusMinutes(minutos));
            notificacao.setProgramadaPara(notificacao.getProximaTentativaEm());
        }
        notificacaoRepository.save(notificacao);
    }

    private void programarLembretes(AgendamentoEntity agendamento) {
        if (!possuiEmail(agendamento.getCliente())) return;
        LocalDateTime inicio = agendamento.getDataHoraInicio();
        criarLembrete(agendamento, TipoNotificacao.LEMBRETE_2H, inicio.minusHours(2));
    }

    private void criarLembrete(AgendamentoEntity agendamento, TipoNotificacao tipo, LocalDateTime programada) {
        NotificacaoEntity existente = notificacaoRepository.findByChaveIdempotencia(chave(agendamento, tipo)).orElse(null);
        if (existente != null) {
            if (existente.getStatus() == StatusNotificacao.CANCELADA
                    && existente.getErroSeguro() != null && existente.getErroSeguro().contains("passado")
                    && programada.isAfter(agora())) {
                existente.setStatus(StatusNotificacao.PENDENTE);
                existente.setProgramadaPara(programada);
                existente.setErroSeguro(null);
                existente.setQuantidadeTentativas(0);
                existente.setDestinatario(agendamento.getCliente().getEmail());
                notificacaoRepository.save(existente);
            }
            return;
        }
        LocalDateTime instante = programada.isAfter(agora()) ? programada : agora();
        NotificacaoEntity entity = criarNotificacao(agendamento, tipo, instante, inicioPara(tipo, agendamento));
        if (!programada.isAfter(agora()) && entity != null) {
            entity.setStatus(StatusNotificacao.CANCELADA);
            entity.setErroSeguro("Horário do lembrete já havia passado; envio retroativo ignorado.");
            notificacaoRepository.save(entity);
        }
    }

    private LocalDateTime inicioPara(TipoNotificacao tipo, AgendamentoEntity agendamento) {
        return agendamento.getDataHoraInicio();
    }

    private NotificacaoEntity criarNotificacao(AgendamentoEntity agendamento, TipoNotificacao tipo,
                                                LocalDateTime programada, LocalDateTime dataHoraAtendimento) {
        if (!possuiEmail(agendamento.getCliente())) return null;
        String chaveIdempotencia = chave(agendamento, tipo);
        NotificacaoEntity existente = notificacaoRepository.findByChaveIdempotencia(chaveIdempotencia).orElse(null);
        if (existente != null) return existente;
        ClienteEntity cliente = agendamento.getCliente();
        NotificacaoEntity notificacao = new NotificacaoEntity();
        notificacao.setClienteId(cliente.getId());
        notificacao.setAgendamentoId(agendamento.getId());
        notificacao.setTipo(tipo);
        notificacao.setCanal(CanalNotificacao.EMAIL.name());
        notificacao.setChaveIdempotencia(chaveIdempotencia);
        notificacao.setDestinatario(cliente.getEmail());
        notificacao.setNomeCliente(cliente.getNome());
        notificacao.setNomeBarbearia(barbeariaRepository.findFirstByOrderByIdAsc()
                .map(b -> b.getNomeFantasia()).orElse(""));
        notificacao.setNomeServico(agendamento.getServico().getNome());
        notificacao.setNomeBarbeiro(agendamento.getBarbeiro().getNome());
        notificacao.setDataHoraAgendamento(dataHoraAtendimento);
        notificacao.setProgramadaPara(programada);
        notificacao.setCriadaEm(agora());
        if (notificacao.getDestinatario() == null || notificacao.getDestinatario().isBlank()) {
            notificacao.setStatus(StatusNotificacao.FALHA);
            notificacao.setErroSeguro("Contato do canal selecionado está ausente.");
        }
        return notificacaoRepository.save(notificacao);
    }

    private String chave(AgendamentoEntity agendamento, TipoNotificacao tipo) {
        return "agendamento:" + agendamento.getId() + ":" + tipo + ":" + agendamento.getDataHoraInicio();
    }

    private void cancelarLembretes(Long agendamentoId, String motivo) {
        notificacaoRepository.cancelarPendentes(agendamentoId, StatusNotificacao.PENDENTE,
                StatusNotificacao.CANCELADA, TIPOS_LEMBRETE, motivo);
    }

    private void preencherDestinatarioAtual(NotificacaoEntity notificacao, ClienteEntity cliente) {
        notificacao.setCanal(CanalNotificacao.EMAIL.name());
        notificacao.setDestinatario(cliente.getEmail());
        if (notificacao.getDestinatario() == null || notificacao.getDestinatario().isBlank()) {
            notificacao.setStatus(StatusNotificacao.FALHA);
            notificacao.setErroSeguro("Contato do canal selecionado está ausente.");
        }
    }

    private void validarContato(ClienteEntity cliente, CanalNotificacao canal) {
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new BadRequestException("Cadastre um e-mail para selecionar este canal.");
        }
    }

    private String obterDestinatario(ClienteEntity cliente, CanalNotificacao canal) {
        return cliente.getEmail();
    }

    private boolean possuiEmail(ClienteEntity cliente) {
        return cliente != null && cliente.getEmail() != null && !cliente.getEmail().isBlank();
    }

    private ClienteEntity clienteAutenticado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || autenticacao.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))
                || !(autenticacao.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new AccessDeniedException("Somente o cliente autenticado pode acessar suas notificações.");
        }
        return clienteRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    private LocalDateTime agora() { return LocalDateTime.now(relogio); }
}
