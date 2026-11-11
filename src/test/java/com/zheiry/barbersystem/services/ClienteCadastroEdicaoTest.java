package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.cliente.ClienteAtualizaDto;
import com.zheiry.barbersystem.dto.cliente.ClienteDto;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.repositories.ClienteRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteCadastroEdicaoTest {

    @Mock ClienteRepository repository;
    @Mock PasswordEncoder passwordEncoder;

    private ClienteService service;
    private Validator validator;

    @BeforeEach
    void setUp() {
        service = new ClienteService(repository, passwordEncoder);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
        validator = null;
    }

    @Test
    void cadastraClienteValidoComSenhaCriptografada() {
        ClienteDto dto = novoCliente("maria@example.test", "Maria Exemplo", "(11) 99999-1234", "Senha#2026A");
        when(repository.save(any(ClienteEntity.class))).thenAnswer(invocacao -> {
            ClienteEntity salvo = invocacao.getArgument(0);
            salvo.setId(15L);
            return salvo;
        });
        when(passwordEncoder.encode(dto.senha())).thenReturn("hash-seguro");

        var saida = service.cadastroCliente(dto);

        assertEquals(15L, saida.id());
        assertEquals("Maria Exemplo", saida.nome());
        verify(repository).save(argThat(cliente -> cliente.getSenha().equals("hash-seguro")
                && cliente.getNivelAcesso() == NivelAcesso.CLIENTE && cliente.getStatus() == Status.ATIVO));
    }

    @Test
    void rejeitaCadastroComCamposAusentesOuSenhaFracaNaValidacao() {
        ClienteDto ausente = new ClienteDto("", null, "", "", "", "");
        ClienteDto senhaFraca = novoCliente("maria@example.test", "Maria Exemplo", "(11) 99999-1234", "fraca");

        assertFalse(validator.validate(ausente).isEmpty());
        assertTrue(validator.validate(senhaFraca).stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("senha")));
        verifyNoInteractions(repository, passwordEncoder);
    }

    @Test
    void atualizaClienteEVerificaPersistenciaDoCampoAlterado() {
        ClienteEntity cliente = clienteExistente();
        autenticar("maria@example.test");
        when(repository.findById(15L)).thenReturn(Optional.of(cliente));
        when(repository.save(any(ClienteEntity.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        var resposta = service.atualizarInformacoesCliente(15L,
                new ClienteAtualizaDto("Maria Atualizada", null, null, null));

        assertEquals("Maria Atualizada", cliente.getNome());
        assertEquals("Maria Atualizada", resposta.nome());
        verify(repository).save(cliente);
    }

    @Test
    void edicaoDeClienteInexistenteRetornaErroTratado() {
        autenticar("maria@example.test");
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.atualizarInformacoesCliente(999L, new ClienteAtualizaDto("Nome Novo", null, null, null)));
        verify(repository, never()).save(any());
    }

    private ClienteDto novoCliente(String email, String nome, String telefone, String senha) {
        return new ClienteDto(nome, LocalDate.of(1995, 5, 10), telefone, email, senha, senha);
    }

    private ClienteEntity clienteExistente() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(15L);
        cliente.setNome("Maria Exemplo");
        cliente.setDataNascimento(LocalDate.of(1995, 5, 10));
        cliente.setTelefone("11999991234");
        cliente.setEmail("maria@example.test");
        cliente.setSenha("hash");
        cliente.setNivelAcesso(NivelAcesso.CLIENTE);
        cliente.setStatus(Status.ATIVO);
        return cliente;
    }

    private void autenticar(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(email, "", java.util.List.of()));
    }
}
