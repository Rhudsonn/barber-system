package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.HorarioFuncionamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioFuncionamentoRepository extends JpaRepository<HorarioFuncionamentoEntity, Long> {


}
