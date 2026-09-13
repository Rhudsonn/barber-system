package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.ServicoEntity;
import com.zheiry.barbersystem.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);


    List<ServicoEntity> findByStatus(Status status);

    List<ServicoEntity> findByNomeContainingIgnoreCase(String nome);

//    List<ServicoEntity> findByCategoriaContainingIgnoreCase(String categoria);


}
