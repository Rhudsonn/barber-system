package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.BarbeariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarbeariaRepository extends JpaRepository<BarbeariaEntity, Long> {


}
