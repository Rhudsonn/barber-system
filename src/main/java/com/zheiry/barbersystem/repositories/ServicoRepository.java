package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {



}
