package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.AgendamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoEntity, Long> {



}
