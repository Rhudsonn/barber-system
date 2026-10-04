package com.zheiry.barbersystem.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Horario_Funcionamento")
public class HorarioFuncionamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



}
