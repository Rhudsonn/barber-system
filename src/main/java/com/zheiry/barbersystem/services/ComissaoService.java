package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.dto.comissao.*;
import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.entities.ComissaoEntity;
import com.zheiry.barbersystem.entities.ServicoEntity;
import com.zheiry.barbersystem.repositories.ComissaoRepository;
import com.zheiry.barbersystem.exception.BadRequestException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComissaoService {

    private static final BigDecimal CEM = new BigDecimal("100.00");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final ComissaoRepository repository;
    private final ZoneId zonaHoraria;

    public ComissaoService(ComissaoRepository repository,
                           @Value("${app.time-zone:America/Sao_Paulo}") String zonaHoraria) {
        this.repository = repository;
        this.zonaHoraria = ZoneId.of(zonaHoraria);
    }

    @Transactional
    public void registrarConclusao(AgendamentoEntity agendamento, ServicoEntity servicoAdicional) {
        LocalDateTime concluidoEm = LocalDateTime.now(zonaHoraria);
        registrarServicoConcluido(agendamento, agendamento.getServico(), concluidoEm);
        if (servicoAdicional != null) {
            registrarServicoConcluido(agendamento, servicoAdicional, concluidoEm);
        }
    }

    private void registrarServicoConcluido(AgendamentoEntity agendamento, ServicoEntity servico,
                                           LocalDateTime concluidoEm) {
        BigDecimal valorServico = BigDecimal.valueOf(servico.getPreco())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal porcentagemBarbearia = servico.getPorcentagemBarbearia();
        if (porcentagemBarbearia == null) {
            porcentagemBarbearia = new BigDecimal("50.00");
        }
        BigDecimal porcentagemBarbeiro = CEM.subtract(porcentagemBarbearia);
        BigDecimal valorBarbearia = valorServico.multiply(porcentagemBarbearia)
                .divide(CEM, 2, RoundingMode.HALF_UP);
        // O profissional recebe o restante para garantir que a soma feche exatamente o preço.
        BigDecimal valorBarbeiro = valorServico.subtract(valorBarbearia);

        ComissaoEntity comissao = new ComissaoEntity();
        comissao.setAgendamentoId(agendamento.getId());
        comissao.setBarbeiroId(agendamento.getBarbeiro().getId());
        comissao.setBarbeiroNome(agendamento.getBarbeiro().getNome());
        comissao.setServicoId(servico.getId());
        comissao.setServicoNome(servico.getNome());
        comissao.setConcluidoEm(concluidoEm);
        comissao.setDataConclusao(concluidoEm.toLocalDate());
        comissao.setValorServico(valorServico);
        comissao.setPorcentagemBarbearia(porcentagemBarbearia);
        comissao.setPorcentagemBarbeiro(porcentagemBarbeiro);
        comissao.setValorBarbearia(valorBarbearia);
        comissao.setValorBarbeiro(valorBarbeiro);
        repository.save(comissao);
    }

    @Transactional
    public void removerConclusao(Long agendamentoId) {
        repository.deleteByAgendamentoId(agendamentoId);
    }

    @Transactional
    public RelatorioComissoesAdministradorDto relatorioAdministrador(LocalDate data, Long barbeiroId) {
        exigirAdministrador();
        LocalDate dataReferencia = data != null ? data : LocalDate.now(zonaHoraria);
        List<ComissaoEntity> comissoes = barbeiroId == null
                ? repository.findByDataConclusaoOrderByBarbeiroNomeAscServicoNomeAsc(dataReferencia)
                : repository.findByDataConclusaoAndBarbeiroIdOrderByServicoNomeAsc(dataReferencia, barbeiroId);

        BigDecimal valorServicos = comissoes.stream().map(ComissaoEntity::getValorServico)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal valorBarbearia = comissoes.stream().map(ComissaoEntity::getValorBarbearia)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal valorComissoes = comissoes.stream().map(ComissaoEntity::getValorBarbeiro)
                .reduce(ZERO, BigDecimal::add);
        Map<Long, List<ComissaoEntity>> porBarbeiro = comissoes.stream()
                .collect(Collectors.groupingBy(ComissaoEntity::getBarbeiroId));
        List<ComissaoBarbeiroResumoDto> barbeiros = porBarbeiro.values().stream()
                .map(this::resumirBarbeiro)
                .sorted((a, b) -> a.barbeiroNome().compareToIgnoreCase(b.barbeiroNome()))
                .toList();

        return new RelatorioComissoesAdministradorDto(dataReferencia, comissoes.size(), valorServicos,
                valorBarbearia, valorComissoes,
                barbeiros, comissoes.stream().map(this::paraDetalheAdministrador).toList());
    }

    @Transactional
    public RelatorioPeriodoComissoesAdministradorDto relatorioAdministradorPeriodo(
            LocalDate inicio, LocalDate fim, Long barbeiroId) {
        exigirAdministrador();
        validarPeriodo(inicio, fim);
        List<ComissaoEntity> comissoes = barbeiroId == null
                ? repository.findByDataConclusaoBetweenOrderByBarbeiroNomeAscServicoNomeAsc(inicio, fim)
                : repository.findByDataConclusaoBetweenAndBarbeiroIdOrderByServicoNomeAsc(inicio, fim, barbeiroId);
        BigDecimal valorServicos = comissoes.stream().map(ComissaoEntity::getValorServico).reduce(ZERO, BigDecimal::add);
        BigDecimal valorBarbearia = comissoes.stream().map(ComissaoEntity::getValorBarbearia).reduce(ZERO, BigDecimal::add);
        BigDecimal valorComissoes = comissoes.stream().map(ComissaoEntity::getValorBarbeiro).reduce(ZERO, BigDecimal::add);
        List<ComissaoBarbeiroResumoDto> barbeiros = comissoes.stream()
                .collect(Collectors.groupingBy(ComissaoEntity::getBarbeiroId)).values().stream()
                .map(this::resumirBarbeiro)
                .sorted((a, b) -> a.barbeiroNome().compareToIgnoreCase(b.barbeiroNome())).toList();
        return new RelatorioPeriodoComissoesAdministradorDto(inicio, fim, comissoes.size(), valorServicos,
                valorBarbearia, valorComissoes, barbeiros,
                comissoes.stream().map(this::paraDetalheAdministrador).toList());
    }

    @Transactional
    public RelatorioPeriodoComissoesBarbeiroDto relatorioDoBarbeiroPeriodo(LocalDate inicio, LocalDate fim) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_BARBEIRO"))
                || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new AccessDeniedException("Apenas o barbeiro autenticado pode consultar suas comissões.");
        }
        validarPeriodo(inicio, fim);
        List<ComissaoEntity> comissoes = repository
                .findByDataConclusaoBetweenAndBarbeiroIdOrderByServicoNomeAsc(inicio, fim, usuario.getId());
        BigDecimal valorServicos = comissoes.stream().map(ComissaoEntity::getValorServico).reduce(ZERO, BigDecimal::add);
        BigDecimal gerada = comissoes.stream().map(ComissaoEntity::getValorBarbeiro).reduce(ZERO, BigDecimal::add);
        return new RelatorioPeriodoComissoesBarbeiroDto(inicio, fim, comissoes.size(), valorServicos, gerada,
                comissoes.stream().map(this::paraDetalheBarbeiro).toList());
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio) || fim.isAfter(inicio.plusYears(1))) {
            throw new BadRequestException("Informe um período válido de até 12 meses.");
        }
    }

    @Transactional
    public RelatorioComissoesBarbeiroDto relatorioDoBarbeiro(LocalDate data) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_BARBEIRO"))
                || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new AccessDeniedException("Apenas o barbeiro autenticado pode consultar suas comissões.");
        }

        LocalDate dataReferencia = data != null ? data : LocalDate.now(zonaHoraria);
        List<ComissaoEntity> comissoes = repository
                .findByDataConclusaoAndBarbeiroIdOrderByServicoNomeAsc(dataReferencia, usuario.getId());
        BigDecimal valorServicos = comissoes.stream().map(ComissaoEntity::getValorServico)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal gerada = comissoes.stream().map(ComissaoEntity::getValorBarbeiro)
                .reduce(ZERO, BigDecimal::add);
        return new RelatorioComissoesBarbeiroDto(dataReferencia, comissoes.size(), valorServicos, gerada,
                comissoes.stream().map(this::paraDetalheBarbeiro).toList());
    }

    private void exigirAdministrador() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"))) {
            throw new AccessDeniedException("Apenas administradores podem consultar o relatório geral de comissões.");
        }
    }

    private ComissaoBarbeiroResumoDto resumirBarbeiro(List<ComissaoEntity> comissoes) {
        ComissaoEntity primeiro = comissoes.get(0);
        return new ComissaoBarbeiroResumoDto(primeiro.getBarbeiroId(), primeiro.getBarbeiroNome(),
                comissoes.size(),
                comissoes.stream().map(ComissaoEntity::getValorServico).reduce(ZERO, BigDecimal::add),
                comissoes.stream().map(ComissaoEntity::getValorBarbeiro).reduce(ZERO, BigDecimal::add));
    }

    private ComissaoDetalheAdministradorDto paraDetalheAdministrador(ComissaoEntity c) {
        return new ComissaoDetalheAdministradorDto(c.getId(), c.getAgendamentoId(), c.getBarbeiroId(),
                c.getBarbeiroNome(), c.getServicoId(), c.getServicoNome(), c.getConcluidoEm(), c.getValorServico(),
                c.getPorcentagemBarbearia(), c.getPorcentagemBarbeiro(), c.getValorBarbearia(), c.getValorBarbeiro());
    }

    private ComissaoDetalheBarbeiroDto paraDetalheBarbeiro(ComissaoEntity c) {
        return new ComissaoDetalheBarbeiroDto(c.getId(), c.getAgendamentoId(), c.getServicoId(), c.getServicoNome(),
                c.getConcluidoEm(), c.getValorServico(), c.getPorcentagemBarbeiro(), c.getValorBarbeiro());
    }
}
