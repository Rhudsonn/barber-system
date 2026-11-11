package com.zheiry.barbersystem.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comissoes", indexes = @Index(name = "idx_comissao_data_barbeiro", columnList = "data_conclusao, barbeiro_id"))
public class ComissaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agendamento_id", nullable = false)
    private Long agendamentoId;

    @Column(name = "barbeiro_id", nullable = false)
    private Long barbeiroId;

    @Column(name = "barbeiro_nome", nullable = false, length = 150)
    private String barbeiroNome;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(name = "servico_nome", nullable = false, length = 150)
    private String servicoNome;

    @Column(name = "data_conclusao", nullable = false)
    private LocalDate dataConclusao;

    @Column(name = "concluido_em", nullable = false)
    private LocalDateTime concluidoEm;

    @Column(name = "valor_servico", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorServico;

    @Column(name = "porcentagem_barbearia", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentagemBarbearia;

    @Column(name = "porcentagem_barbeiro", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentagemBarbeiro;

    @Column(name = "valor_barbearia", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorBarbearia;

    @Column(name = "valor_barbeiro", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorBarbeiro;

    public Long getId() { return id; }
    public Long getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(Long agendamentoId) { this.agendamentoId = agendamentoId; }
    public Long getBarbeiroId() { return barbeiroId; }
    public void setBarbeiroId(Long barbeiroId) { this.barbeiroId = barbeiroId; }
    public String getBarbeiroNome() { return barbeiroNome; }
    public void setBarbeiroNome(String barbeiroNome) { this.barbeiroNome = barbeiroNome; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public String getServicoNome() { return servicoNome; }
    public void setServicoNome(String servicoNome) { this.servicoNome = servicoNome; }
    public LocalDate getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDate dataConclusao) { this.dataConclusao = dataConclusao; }
    public LocalDateTime getConcluidoEm() { return concluidoEm; }
    public void setConcluidoEm(LocalDateTime concluidoEm) { this.concluidoEm = concluidoEm; }
    public BigDecimal getValorServico() { return valorServico; }
    public void setValorServico(BigDecimal valorServico) { this.valorServico = valorServico; }
    public BigDecimal getPorcentagemBarbearia() { return porcentagemBarbearia; }
    public void setPorcentagemBarbearia(BigDecimal porcentagemBarbearia) { this.porcentagemBarbearia = porcentagemBarbearia; }
    public BigDecimal getPorcentagemBarbeiro() { return porcentagemBarbeiro; }
    public void setPorcentagemBarbeiro(BigDecimal porcentagemBarbeiro) { this.porcentagemBarbeiro = porcentagemBarbeiro; }
    public BigDecimal getValorBarbearia() { return valorBarbearia; }
    public void setValorBarbearia(BigDecimal valorBarbearia) { this.valorBarbearia = valorBarbearia; }
    public BigDecimal getValorBarbeiro() { return valorBarbeiro; }
    public void setValorBarbeiro(BigDecimal valorBarbeiro) { this.valorBarbeiro = valorBarbeiro; }
}
