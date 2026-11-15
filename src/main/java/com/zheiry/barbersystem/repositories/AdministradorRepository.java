package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.AdministradorEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<AdministradorEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AdministradorEntity a where a.id = :id")
    Optional<AdministradorEntity> findByIdForUpdate(@Param("id") Long id);

    Optional<AdministradorEntity> findByEmail(String email);
    Optional<AdministradorEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefoneAndIdNot(String telefone, Long id);
}
