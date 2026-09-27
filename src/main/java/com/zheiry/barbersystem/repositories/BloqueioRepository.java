package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.BloqueioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloqueioRepository extends JpaRepository<BloqueioEntity, Long> {


    // Busca bloqueios do barbeiro que possam se sobrepor ao novo período
    List<BloqueioEntity> findByBarbeiroIdAndDataInicioLessThanAndDataFimGreaterThan(
            Long barbeiroId,
            LocalDateTime dataFim,      // novoFim
            LocalDateTime dataInicio    // novoInicio
    );

}
