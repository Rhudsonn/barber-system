package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.config.PostgresTestBase;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BarbeiroRepositoryIntegracaoTest extends PostgresTestBase {

    @Autowired
    private BarbeiroRepository repository;

    @Test
    void consegueObterBloqueioPessimistaDaAgendaDoBarbeiro() {
        BarbeiroEntity barbeiro = new BarbeiroEntity();
        barbeiro.setNome("Barbeiro de teste");
        barbeiro.setDataNascimento(LocalDate.of(1990, 1, 1));
        barbeiro.setTelefone("11999990000");
        barbeiro.setEspecialidade("Corte");
        barbeiro.setEmail("barbeiro@example.test");
        barbeiro.setSenha("hash-de-teste");
        barbeiro.setNivelAcesso(NivelAcesso.BARBEIRO);
        barbeiro.setStatus(Status.ATIVO);
        Long id = repository.saveAndFlush(barbeiro).getId();

        assertTrue(repository.findByIdForUpdate(id).isPresent());
    }
}
