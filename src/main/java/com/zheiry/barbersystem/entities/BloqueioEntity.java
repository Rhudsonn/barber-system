package com.zheiry.barbersystem.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "bloqueio")
public class BloqueioEntity {

    // Essa tabela vai com - (Folgas, Almoço, Ferias).---------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Um barbeiro pode ter vários bloqueios, mas cada bloqueio pertence a um único barbeiro.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private BarbeiroEntity barbeiro;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false)
    private String motivo;


    public BloqueioEntity() {
    }

    public BloqueioEntity(Long id, BarbeiroEntity barbeiro, LocalDate dataInicio, LocalDate dataFim, String motivo) {
        this.id = id;
        this.barbeiro = barbeiro;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.motivo = motivo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BarbeiroEntity getBarbeiro() {
        return barbeiro;
    }

    public void setBarbeiro(BarbeiroEntity barbeiro) {
        this.barbeiro = barbeiro;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
