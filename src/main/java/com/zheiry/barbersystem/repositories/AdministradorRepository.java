package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.AdministradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<AdministradorEntity, Long> {


    Optional<AdministradorEntity> findByEmail(String email);
    Optional<AdministradorEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByTelefoneAndIdNot(String telefone, Long id);
}
