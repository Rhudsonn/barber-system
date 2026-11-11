package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.BloqueioEntity;
import com.zheiry.barbersystem.repositories.AgendamentoRepository;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.repositories.BloqueioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloqueioServicePermissaoTest {
    @Mock BloqueioRepository repository;
    @Mock BarbeiroRepository barbeiroRepository;
    @Mock AgendamentoRepository agendamentoRepository;
    @InjectMocks BloqueioService service;

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void barbeiroNaoPodeListarBloqueiosDeOutraAgenda() {
        autenticar(17L, "BARBEIRO");

        assertThrows(AccessDeniedException.class, () -> service.listarPorBarbeiro(18L));
    }

    @Test
    void barbeiroNaoPodeConsultarBloqueioDeOutraAgenda() {
        autenticar(17L, "BARBEIRO");
        BarbeiroEntity barbeiro = new BarbeiroEntity();
        barbeiro.setId(18L);
        BloqueioEntity bloqueio = new BloqueioEntity();
        bloqueio.setBarbeiro(barbeiro);
        when(repository.findById(1L)).thenReturn(Optional.of(bloqueio));

        assertThrows(AccessDeniedException.class, () -> service.buscarPorId(1L));
    }

    private void autenticar(Long id, String perfil) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
        var usuario = new UsuarioAutenticado(id, "teste@local", "senha-nao-utilizada", authorities, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, authorities));
    }
}
