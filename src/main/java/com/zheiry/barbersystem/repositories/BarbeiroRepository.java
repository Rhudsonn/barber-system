package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.BarbeiroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarbeiroRepository extends JpaRepository<BarbeiroEntity, Long> {

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);


}
