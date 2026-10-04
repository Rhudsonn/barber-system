package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.BloqueioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloqueioRepository extends JpaRepository<BloqueioEntity, Long> {

    // Listar todos os bloqueios de um barbeiro
    List<BloqueioEntity> findByBarbeiroId(Long barbeiroId);

    // Busca bloqueios do barbeiro que possam se sobrepor ao novo período
    List<BloqueioEntity> findByBarbeiroIdAndDataInicioLessThanAndDataFimGreaterThan(
            Long barbeiroId,
            LocalDateTime dataFim,      // novoFim
            LocalDateTime dataInicio    // novoInicio
    );

    // Versão para atualizar: sobreposição ignorando o próprio id
    List<BloqueioEntity> findByBarbeiroIdAndIdNotAndDataInicioLessThanAndDataFimGreaterThan(
            Long barbeiroId,
            Long id,           // ignora o próprio
            LocalDateTime dataFim,
            LocalDateTime dataInicio
    );

    boolean existsByBarbeiroIdAndDataInicioLessThanAndDataFimGreaterThan(
            Long barbeiroId, LocalDateTime dataFim, LocalDateTime dataInicio);

}
