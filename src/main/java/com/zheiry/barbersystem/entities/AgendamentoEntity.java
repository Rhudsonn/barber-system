package com.zheiry.barbersystem.entities;

import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.enums.FormaPagamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos", indexes = {
        @Index(name = "idx_barbeiro_data", columnList = "barbeiro_id, data_hora_inicio"),
        @Index(name = "idx_cliente", columnList = "cliente_id"),
        @Index(name = "idx_status_data", columnList = "status_agendamento, data_hora_inicio")
})
public class AgendamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamentos (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private ClienteEntity cliente;

    @Column(name = "cliente_avulso_nome")
    private String clienteAvulsoNome;

    @Column(name = "cliente_avulso_telefone", length = 255)
    private String clienteAvulsoTelefone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private BarbeiroEntity barbeiro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private ServicoEntity servico;

    // Datas e horários (use LocalDateTime)
    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;   // calculado a partir da duração do serviço

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento statusAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "valor_total_comanda", precision = 12, scale = 2)
    private BigDecimal valorTotalComanda;

    @Column(length = 500)
    private String observacao;

    // Auditoria (muito útil em produção)
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public AgendamentoEntity() {
    }

    public AgendamentoEntity(Long id, ClienteEntity cliente, BarbeiroEntity barbeiro, ServicoEntity servico, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, StatusAgendamento statusAgendamento, String observacao, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.cliente = cliente;
        this.barbeiro = barbeiro;
        this.servico = servico;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.statusAgendamento = statusAgendamento;
        this.observacao = observacao;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteEntity getCliente() {
        return cliente;
    }

    public void setCliente(ClienteEntity cliente) {
        this.cliente = cliente;
    }

    public String getClienteAvulsoNome() { return clienteAvulsoNome; }
    public void setClienteAvulsoNome(String clienteAvulsoNome) { this.clienteAvulsoNome = clienteAvulsoNome; }
    public String getClienteAvulsoTelefone() { return clienteAvulsoTelefone; }
    public void setClienteAvulsoTelefone(String clienteAvulsoTelefone) { this.clienteAvulsoTelefone = clienteAvulsoTelefone; }

    public BarbeiroEntity getBarbeiro() {
        return barbeiro;
    }

    public void setBarbeiro(BarbeiroEntity barbeiro) {
        this.barbeiro = barbeiro;
    }

    public ServicoEntity getServico() {
        return servico;
    }

    public void setServico(ServicoEntity servico) {
        this.servico = servico;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public StatusAgendamento getStatusAgendamento() {
        return statusAgendamento;
    }

    public void setStatusAgendamento(StatusAgendamento statusAgendamento) {
        this.statusAgendamento = statusAgendamento;
    }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public BigDecimal getValorTotalComanda() { return valorTotalComanda; }
    public void setValorTotalComanda(BigDecimal valorTotalComanda) { this.valorTotalComanda = valorTotalComanda; }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
