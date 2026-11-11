package com.zheiry.barbersystem.entities;

import com.zheiry.barbersystem.enums.CanalNotificacao;
import com.zheiry.barbersystem.enums.StatusNotificacao;
import com.zheiry.barbersystem.enums.TipoNotificacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes",
        uniqueConstraints = @UniqueConstraint(name = "uk_notificacao_idempotencia", columnNames = "chave_idempotencia"),
        indexes = @Index(name = "idx_notificacao_fila", columnList = "status, programada_para"))
public class NotificacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "agendamento_id", nullable = false)
    private Long agendamentoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotificacao canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusNotificacao status = StatusNotificacao.PENDENTE;

    @Column(name = "chave_idempotencia", nullable = false, length = 180)
    private String chaveIdempotencia;

    @Column(name = "destinatario", length = 254)
    private String destinatario;

    @Column(name = "nome_cliente", nullable = false, length = 150)
    private String nomeCliente;

    @Column(name = "nome_barbearia", nullable = false, length = 150)
    private String nomeBarbearia;

    @Column(name = "nome_servico", nullable = false, length = 150)
    private String nomeServico;

    @Column(name = "nome_barbeiro", nullable = false, length = 150)
    private String nomeBarbeiro;

    @Column(name = "data_hora_agendamento", nullable = false)
    private LocalDateTime dataHoraAgendamento;

    @Column(name = "programada_para", nullable = false)
    private LocalDateTime programadaPara;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "tentada_em")
    private LocalDateTime tentadaEm;

    @Column(name = "enviada_em")
    private LocalDateTime enviadaEm;

    @Column(name = "proxima_tentativa_em")
    private LocalDateTime proximaTentativaEm;

    @Column(name = "quantidade_tentativas", nullable = false)
    private int quantidadeTentativas;

    @Column(name = "codigo_provedor", length = 200)
    private String codigoProvedor;

    @Column(name = "erro_seguro", length = 500)
    private String erroSeguro;

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(Long agendamentoId) { this.agendamentoId = agendamentoId; }
    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }
    public CanalNotificacao getCanal() { return canal; }
    public void setCanal(CanalNotificacao canal) { this.canal = canal; }
    public StatusNotificacao getStatus() { return status; }
    public void setStatus(StatusNotificacao status) { this.status = status; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public String getNomeBarbearia() { return nomeBarbearia; }
    public void setNomeBarbearia(String nomeBarbearia) { this.nomeBarbearia = nomeBarbearia; }
    public String getNomeServico() { return nomeServico; }
    public void setNomeServico(String nomeServico) { this.nomeServico = nomeServico; }
    public String getNomeBarbeiro() { return nomeBarbeiro; }
    public void setNomeBarbeiro(String nomeBarbeiro) { this.nomeBarbeiro = nomeBarbeiro; }
    public LocalDateTime getDataHoraAgendamento() { return dataHoraAgendamento; }
    public void setDataHoraAgendamento(LocalDateTime dataHoraAgendamento) { this.dataHoraAgendamento = dataHoraAgendamento; }
    public LocalDateTime getProgramadaPara() { return programadaPara; }
    public void setProgramadaPara(LocalDateTime programadaPara) { this.programadaPara = programadaPara; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public LocalDateTime getTentadaEm() { return tentadaEm; }
    public void setTentadaEm(LocalDateTime tentadaEm) { this.tentadaEm = tentadaEm; }
    public LocalDateTime getEnviadaEm() { return enviadaEm; }
    public void setEnviadaEm(LocalDateTime enviadaEm) { this.enviadaEm = enviadaEm; }
    public LocalDateTime getProximaTentativaEm() { return proximaTentativaEm; }
    public void setProximaTentativaEm(LocalDateTime proximaTentativaEm) { this.proximaTentativaEm = proximaTentativaEm; }
    public int getQuantidadeTentativas() { return quantidadeTentativas; }
    public void setQuantidadeTentativas(int quantidadeTentativas) { this.quantidadeTentativas = quantidadeTentativas; }
    public String getCodigoProvedor() { return codigoProvedor; }
    public void setCodigoProvedor(String codigoProvedor) { this.codigoProvedor = codigoProvedor; }
    public String getErroSeguro() { return erroSeguro; }
    public void setErroSeguro(String erroSeguro) { this.erroSeguro = erroSeguro; }
}
