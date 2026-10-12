package com.zheiry.barbersystem.entities;

import com.zheiry.barbersystem.enums.DiaSemana;
import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "Horario_Funcionamento")
public class HorarioFuncionamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbearia_id", nullable = false)
    private BarbeariaEntity barbearia;

    @Enumerated(EnumType.STRING)
    private DiaSemana diaSemana;

    private LocalTime horarioInicio;
    private LocalTime horarioFim;

    public HorarioFuncionamentoEntity() {
    }

    public HorarioFuncionamentoEntity(Long id, BarbeariaEntity barbearia, DiaSemana diaSemana, LocalTime horarioInicio, LocalTime horarioFim) {
        this.id = id;
        this.barbearia = barbearia;
        this.diaSemana = diaSemana;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BarbeariaEntity getBarbearia() {
        return barbearia;
    }

    public void setBarbearia(BarbeariaEntity barbearia) {
        this.barbearia = barbearia;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }
}

