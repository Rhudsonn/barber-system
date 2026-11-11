package com.zheiry.barbersystem.entities;

import com.zheiry.barbersystem.enums.FormaPagamento;
import com.zheiry.barbersystem.enums.TipoHistoricoAgendamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_agendamentos", indexes = @Index(name = "idx_historico_agendamento_data", columnList = "agendamento_id, registrado_em"))
public class HistoricoAgendamentoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agendamento_id", nullable = false)
    private Long agendamentoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 30)
    private TipoHistoricoAgendamento tipoEvento;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_email", length = 255)
    private String usuarioEmail;

    @Column(name = "perfil_usuario", length = 30)
    private String perfilUsuario;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nome", nullable = false, length = 255)
    private String clienteNome;

    @Column(name = "barbeiro_id", nullable = false)
    private Long barbeiroId;

    @Column(name = "barbeiro_nome", nullable = false, length = 255)
    private String barbeiroNome;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(name = "servico_nome", nullable = false, length = 255)
    private String servicoNome;

    @Column(name = "valor_servico", precision = 12, scale = 2)
    private BigDecimal valorServico;

    @Column(name = "servico_adicional_id")
    private Long servicoAdicionalId;

    @Column(name = "servico_adicional_nome", length = 255)
    private String servicoAdicionalNome;

    @Column(name = "valor_servico_adicional", precision = 12, scale = 2)
    private BigDecimal valorServicoAdicional;

    @Column(name = "data_hora_anterior")
    private LocalDateTime dataHoraAnterior;

    @Column(name = "data_hora_nova")
    private LocalDateTime dataHoraNova;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "valor_total_comanda", precision = 12, scale = 2)
    private BigDecimal valorTotalComanda;

    public Long getId() { return id; }
    public Long getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(Long agendamentoId) { this.agendamentoId = agendamentoId; }
    public TipoHistoricoAgendamento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoHistoricoAgendamento tipoEvento) { this.tipoEvento = tipoEvento; }
    public LocalDateTime getRegistradoEm() { return registradoEm; }
    public void setRegistradoEm(LocalDateTime registradoEm) { this.registradoEm = registradoEm; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
    public String getPerfilUsuario() { return perfilUsuario; }
    public void setPerfilUsuario(String perfilUsuario) { this.perfilUsuario = perfilUsuario; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public Long getBarbeiroId() { return barbeiroId; }
    public void setBarbeiroId(Long barbeiroId) { this.barbeiroId = barbeiroId; }
    public String getBarbeiroNome() { return barbeiroNome; }
    public void setBarbeiroNome(String barbeiroNome) { this.barbeiroNome = barbeiroNome; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public String getServicoNome() { return servicoNome; }
    public void setServicoNome(String servicoNome) { this.servicoNome = servicoNome; }
    public BigDecimal getValorServico() { return valorServico; }
    public void setValorServico(BigDecimal valorServico) { this.valorServico = valorServico; }
    public Long getServicoAdicionalId() { return servicoAdicionalId; }
    public void setServicoAdicionalId(Long servicoAdicionalId) { this.servicoAdicionalId = servicoAdicionalId; }
    public String getServicoAdicionalNome() { return servicoAdicionalNome; }
    public void setServicoAdicionalNome(String servicoAdicionalNome) { this.servicoAdicionalNome = servicoAdicionalNome; }
    public BigDecimal getValorServicoAdicional() { return valorServicoAdicional; }
    public void setValorServicoAdicional(BigDecimal valorServicoAdicional) { this.valorServicoAdicional = valorServicoAdicional; }
    public LocalDateTime getDataHoraAnterior() { return dataHoraAnterior; }
    public void setDataHoraAnterior(LocalDateTime dataHoraAnterior) { this.dataHoraAnterior = dataHoraAnterior; }
    public LocalDateTime getDataHoraNova() { return dataHoraNova; }
    public void setDataHoraNova(LocalDateTime dataHoraNova) { this.dataHoraNova = dataHoraNova; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public BigDecimal getValorTotalComanda() { return valorTotalComanda; }
    public void setValorTotalComanda(BigDecimal valorTotalComanda) { this.valorTotalComanda = valorTotalComanda; }
}
