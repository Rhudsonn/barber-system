package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);
}
