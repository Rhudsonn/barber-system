package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarbeiroRepository extends JpaRepository<BarbeiroEntity, Long> {

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);

    Optional<BarbeiroEntity> findByEmail(String email);

    List<BarbeiroEntity> findByStatus(Status status);

}
