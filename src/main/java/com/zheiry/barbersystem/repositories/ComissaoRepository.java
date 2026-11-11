package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.ComissaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ComissaoRepository extends JpaRepository<ComissaoEntity, Long> {

    boolean existsByAgendamentoId(Long agendamentoId);

    void deleteByAgendamentoId(Long agendamentoId);

    List<ComissaoEntity> findByDataConclusaoOrderByBarbeiroNomeAscServicoNomeAsc(LocalDate dataConclusao);

    List<ComissaoEntity> findByDataConclusaoAndBarbeiroIdOrderByServicoNomeAsc(
            LocalDate dataConclusao, Long barbeiroId);

    List<ComissaoEntity> findByDataConclusaoBetweenOrderByBarbeiroNomeAscServicoNomeAsc(
            LocalDate inicio, LocalDate fim);

    List<ComissaoEntity> findByDataConclusaoBetweenAndBarbeiroIdOrderByServicoNomeAsc(
            LocalDate inicio, LocalDate fim, Long barbeiroId);
}
