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

    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefoneAndIdNot(String telefone, Long id);

    Optional<BarbeiroEntity> findByEmail(String email);

    //Me ajuda nos métodos de listar barbeiros com status ativo e inaivo
    List<BarbeiroEntity> findByStatus(Status status);

    //O Containing permite pesquisar parte do nome, e o IgnoreCase faz com que joão, João e JOÃO sejam tratados da mesma forma.
    List<BarbeiroEntity> findByNomeContainingIgnoreCase(String nome);

}
