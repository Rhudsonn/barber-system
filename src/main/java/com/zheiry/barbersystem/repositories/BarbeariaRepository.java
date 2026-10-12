package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.BarbeariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BarbeariaRepository extends JpaRepository<BarbeariaEntity, Long> {

    boolean existsByCnpj(String cnpj);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<BarbeariaEntity> findFirstByOrderByIdAsc();

}
