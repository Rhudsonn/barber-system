package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.enums.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ClienteEntity c where c.id = :id")
    Optional<ClienteEntity> findByIdForUpdate(@Param("id") Long id);

    Optional<ClienteEntity> findByEmail(String email);
    Optional<ClienteEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);

    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefoneAndIdNot(String telefone, Long id);

    //Me ajuda nos métodos de listar clientes com status ativo e inaivo
    List<ClienteEntity> findByStatus(Status status);

    @org.springframework.data.jpa.repository.Query("select c from ClienteEntity c where c.status = :status and month(c.dataNascimento) = :mes and day(c.dataNascimento) = :dia")
    List<ClienteEntity> buscarAniversariantes(@org.springframework.data.repository.query.Param("status") Status status,
                                               @org.springframework.data.repository.query.Param("mes") int mes,
                                               @org.springframework.data.repository.query.Param("dia") int dia);

    //O Containing permite pesquisar parte do nome, e o IgnoreCase faz com que joão, João e JOÃO sejam tratados da mesma forma.
    List<ClienteEntity> findByNomeContainingIgnoreCase(String nome);




}
