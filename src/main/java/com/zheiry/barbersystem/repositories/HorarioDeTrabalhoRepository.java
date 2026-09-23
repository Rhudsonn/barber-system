package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.HorarioDeTrabalhoEntity;
import com.zheiry.barbersystem.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

@Repository
public interface HorarioDeTrabalhoRepository extends JpaRepository<HorarioDeTrabalhoEntity, Long> {


    //Esse método é uma query derivada do Spring Data JPA. Ele permite verificar se existe algum registro no banco que atenda a várias condições, sem você precisar escrever SQL
    boolean existsByBarbeiroIdAndDiaSemanaAndHorarioInicioLessThanAndHorarioFimGreaterThan(
            Long barbeiroId,
            DiaSemana diaSemana,
            LocalTime horarioInicio,
            LocalTime horarioFim
    );
}
