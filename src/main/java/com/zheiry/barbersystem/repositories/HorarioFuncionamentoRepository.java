package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.HorarioFuncionamentoEntity;
import com.zheiry.barbersystem.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface HorarioFuncionamentoRepository extends JpaRepository<HorarioFuncionamentoEntity, Long> {

    List<HorarioFuncionamentoEntity> findByBarbeariaId(Long barbeariaId);

    List<HorarioFuncionamentoEntity> findByBarbeariaIdAndDiaSemana(Long barbeariaId, DiaSemana diaSemana);

    // Detecta sobreposição: novoInicio < fimExistente AND novoFim > inicioExistente
    boolean existsByBarbeariaIdAndDiaSemanaAndHorarioInicioLessThanAndHorarioFimGreaterThan(
            Long barbeariaId,
            DiaSemana diaSemana,
            LocalTime novoFim,
            LocalTime novoInicio
    );

    // Mesma verificação de sobreposição, ignorando o próprio registro (para update)
    boolean existsByBarbeariaIdAndDiaSemanaAndIdNotAndHorarioInicioLessThanAndHorarioFimGreaterThan(
            Long barbeariaId,
            DiaSemana diaSemana,
            Long id,
            LocalTime novoFim,
            LocalTime novoInicio
    );

}
