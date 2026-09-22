package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.HorarioDeTrabalhoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioDeTrabalhoRepository extends JpaRepository<HorarioDeTrabalhoEntity, Long> {


}
