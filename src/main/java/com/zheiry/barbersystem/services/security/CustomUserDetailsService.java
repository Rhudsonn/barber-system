package com.zheiry.barbersystem.services.security;

import com.zheiry.barbersystem.entities.AdministradorEntity;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.repositories.AdministradorRepository;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.repositories.ClienteRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    private final AdministradorRepository administradorRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final ClienteRepository clienteRepository;

    public CustomUserDetailsService(AdministradorRepository administradorRepository, BarbeiroRepository barbeiroRepository, ClienteRepository clienteRepository) {
        this.administradorRepository = administradorRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.clienteRepository = clienteRepository;
    }

    //método principal da UserDetailsServiceinterface. Ele é responsável por
    // recuperar os dados de autenticação e autorização do usuário da sua fonte de dados
    // (por exemplo, um banco de dados relacional) durante o processo de login.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // tenta achar como administrador
        Optional<AdministradorEntity> admin = administradorRepository.findByEmail(email);
        if (admin.isPresent()) {
            //Aquiu chamei o getter sem parametros getStatus() e comparei o valor retornado com o Enum Status.Ativo.
            AdministradorEntity administrador = admin.get();
            boolean isAtivo = Status.ATIVO.equals(administrador.getStatus());

            return toUserDetails(
                    admin.get().getEmail(),
                    admin.get().getSenha(),
                    admin.get().getNivelAcesso().name(), isAtivo
            );
        }

        // se não achou, tenta como barbeiro
        Optional<BarbeiroEntity> barbeiro = barbeiroRepository.findByEmail(email);
        if (barbeiro.isPresent()) {

            BarbeiroEntity barbeiro1 = barbeiro.get();
            boolean isAtivo = Status.ATIVO.equals(barbeiro1.getStatus());

            return toUserDetails(
                    barbeiro.get().getEmail(),
                    barbeiro.get().getSenha(),
                    barbeiro.get().getNivelAcesso().name(),isAtivo
            );
        }

        // Se não achou Administrador e nem Barbeiro, tenta como Cliente
        Optional<ClienteEntity> cliente = clienteRepository.findByEmail(email);
        if (cliente.isPresent()) {

            ClienteEntity cliente1 = cliente.get();
            boolean isAtivo = Status.ATIVO.equals(cliente1.getStatus());

            return toUserDetails(
                    cliente.get().getEmail(),
                    cliente.get().getSenha(),
                    cliente.get().getNivelAcesso().name(), isAtivo
            );

        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + email);
    }

    private UserDetails toUserDetails(String email, String senha, String role, boolean ativo) {
        return User.builder()
                .username(email)
                .password(senha)
                .roles(role)          // Spring adiciona o prefixo ROLE_ automaticamente
                .disabled(!ativo)
                .build();
    }
}
