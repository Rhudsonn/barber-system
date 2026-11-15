package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.RecuperacaoSenhaEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenhaEntity, Long> {

    Optional<RecuperacaoSenhaEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecuperacaoSenhaEntity r where r.tokenHash = :tokenHash")
    Optional<RecuperacaoSenhaEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    void deleteByNivelAcessoAndUsuarioId(NivelAcesso nivelAcesso, Long usuarioId);
}
