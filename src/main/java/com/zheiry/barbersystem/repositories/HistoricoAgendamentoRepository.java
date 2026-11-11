package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.HistoricoAgendamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoAgendamentoRepository extends JpaRepository<HistoricoAgendamentoEntity, Long> {
    List<HistoricoAgendamentoEntity> findByAgendamentoIdOrderByRegistradoEmAsc(Long agendamentoId);
}
