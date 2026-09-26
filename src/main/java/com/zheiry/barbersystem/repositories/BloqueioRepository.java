package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.BloqueioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloqueioRepository extends JpaRepository<BloqueioEntity, Long> {



}
