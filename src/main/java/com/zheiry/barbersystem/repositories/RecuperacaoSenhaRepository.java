package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.RecuperacaoSenhaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenhaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RecuperacaoSenhaEntity> findByTokenHash(String tokenHash);

    void deleteByEmailIgnoreCase(String email);
}
