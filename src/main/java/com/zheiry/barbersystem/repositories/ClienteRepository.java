package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);

    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefoneAndIdNot(String telefone, Long id);

    //Me ajuda nos métodos de listar clientes com status ativo e inaivo
    List<ClienteEntity> findByStatus(Status status);

    //O Containing permite pesquisar parte do nome, e o IgnoreCase faz com que joão, João e JOÃO sejam tratados da mesma forma.
    List<ClienteEntity> findByNomeContainingIgnoreCase(String nome);




}
