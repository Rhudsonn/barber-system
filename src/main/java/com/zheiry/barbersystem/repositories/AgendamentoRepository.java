package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AgendamentoEntity a where a.id = :id")
    java.util.Optional<AgendamentoEntity> findByIdForUpdate(@Param("id") Long id);

    // Lista agendamentos de um cliente (paginado)
    Page<AgendamentoEntity> findByClienteId(Long clienteId, Pageable pageable);

    List<AgendamentoEntity> findByClienteIdAndDataHoraInicioGreaterThanEqualAndDataHoraInicioLessThanOrderByDataHoraInicioAsc(
            Long clienteId, LocalDateTime inicio, LocalDateTime fim);

    List<AgendamentoEntity> findByClienteIdAndStatusAgendamentoIn(Long clienteId, List<StatusAgendamento> status);

    List<AgendamentoEntity> findByDataHoraInicioAfterAndStatusAgendamentoIn(
            LocalDateTime dataHora, List<StatusAgendamento> status);

    // Lista agendamentos de um barbeiro (paginado)
    Page<AgendamentoEntity> findByBarbeiroId(Long barbeiroId, Pageable pageable);

    List<AgendamentoEntity> findByBarbeiroIdAndDataHoraInicioGreaterThanEqualAndDataHoraInicioLessThanOrderByDataHoraInicioAsc(
            Long barbeiroId, LocalDateTime inicio, LocalDateTime fim);

    List<AgendamentoEntity> findByDataHoraInicioGreaterThanEqualAndDataHoraInicioLessThanOrderByDataHoraInicioAsc(
            LocalDateTime inicio, LocalDateTime fim);

    // Lista agendamentos de um barbeiro em um intervalo de data/hora
    List<AgendamentoEntity> findByBarbeiroIdAndDataHoraInicioBetween(
            Long barbeiroId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Busca qualquer agendamento que intersecte [inicio, fim), inclusive os iniciados antes de inicio.
    List<AgendamentoEntity> findByBarbeiroIdAndDataHoraInicioLessThanAndDataHoraFimGreaterThan(
            Long barbeiroId, LocalDateTime fim, LocalDateTime inicio);


    // Lista agendamentos de um barbeiro em um intervalo, excluindo certos status
    List<AgendamentoEntity> findByBarbeiroIdAndDataHoraInicioBetweenAndStatusAgendamentoNotIn(
            Long barbeiroId,
            LocalDateTime inicio,
            LocalDateTime fim,
            List<StatusAgendamento> statusExcluidos
    );

    List<AgendamentoEntity> findByBarbeiroIdAndDataHoraInicioLessThanAndDataHoraFimGreaterThanAndStatusAgendamentoNotIn(
            Long barbeiroId, LocalDateTime fim, LocalDateTime inicio, List<StatusAgendamento> statusExcluidos);

    // Lista por status (útil para admin)
    List<AgendamentoEntity> findByStatusAgendamento(StatusAgendamento status);

    List<AgendamentoEntity> findByDataHoraFimLessThanEqualAndStatusAgendamentoIn(
            LocalDateTime dataHoraFim, List<StatusAgendamento> statusAgendamento);

    // Lista por status (útil para admin)
    Page<AgendamentoEntity> findByStatusAgendamento(StatusAgendamento status, Pageable pageable);

    // Lista de um barbeiro filtrando por status
    Page<AgendamentoEntity> findByBarbeiroIdAndStatusAgendamento(
            Long barbeiroId,
            StatusAgendamento status,
            Pageable pageable
    );

    // Busca agendamentos futuros de um barbeiro, excluindo status que não importam
    // Este metodo me ajuda na classe HorarioDeServicoService me auxiliando na exclusao do horario,
    // com ele verifico se o barbeiro tem agendamento dentro do horario de trabalho futuro antes de esxcluir, e perde os dados.
    // dataHoraInicioAfter - só pega agendamentos futuros
    // StatusAgendamentoNotIn - ignora status que não devem impedir a exclusão
    List<AgendamentoEntity> findByBarbeiroIdAndDataHoraInicioAfterAndStatusAgendamentoNotIn(
            Long barbeiroId,
            LocalDateTime dataHora,
            List<StatusAgendamento> statusExcluidos
    );

}
