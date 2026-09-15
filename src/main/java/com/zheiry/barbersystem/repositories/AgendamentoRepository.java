package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoEntity, Long> {

    // Lista agendamentos de um cliente (paginado)
    Page<AgendamentoEntity> findByClienteId(Long clienteId, Pageable pageable);

    // Lista agendamentos de um barbeiro (paginado)
    Page<AgendamentoEntity> findByBarbeiroId(Long barbeiroId, Pageable pageable);

    // Lista agendamentos de um barbeiro em um intervalo de data/hora
    List<AgendamentoEntity> findByBarbeiroIdAndDataHoraInicioBetween(
            Long barbeiroId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Lista agendamentos de um barbeiro em um intervalo, excluindo certos status
    List<AgendamentoEntity> findByBarbeiroIdAndDataHoraInicioBetweenAndStatusAgendamentoNotIn(
            Long barbeiroId,
            LocalDateTime inicio,
            LocalDateTime fim,
            List<StatusAgendamento> statusExcluidos
    );

    // Lista por status (útil para admin)
    List<AgendamentoEntity> findByStatusAgendamento(StatusAgendamento status);

    // Lista por status (útil para admin)
    Page<AgendamentoEntity> findByStatusAgendamento(StatusAgendamento status, Pageable pageable);

    // Lista de um barbeiro filtrando por status
    Page<AgendamentoEntity> findByBarbeiroIdAndStatusAgendamento(
            Long barbeiroId,
            StatusAgendamento status,
            Pageable pageable
    );

}
