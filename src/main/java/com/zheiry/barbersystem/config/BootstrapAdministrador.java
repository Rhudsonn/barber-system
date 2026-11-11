package com.zheiry.barbersystem.config;

import com.zheiry.barbersystem.entities.AdministradorEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.repositories.AdministradorRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class BootstrapAdministrador implements ApplicationRunner {
    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String telefone;
    private final String email;
    private final String senha;

    public BootstrapAdministrador(
            AdministradorRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.nome:}") String nome,
            @Value("${app.bootstrap-admin.telefone:}") String telefone,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String senha) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;
        if (nome.isBlank() || telefone.isBlank() || email.isBlank() || senha.length() < 12) {
            throw new IllegalStateException("Configure nome, telefone, e-mail e uma senha de pelo menos 12 caracteres para criar o administrador inicial.");
        }

        AdministradorEntity administrador = new AdministradorEntity();
        administrador.setNome(nome.strip());
        administrador.setTelefone(telefone.strip());
        administrador.setEmail(email.strip().toLowerCase());
        administrador.setSenha(passwordEncoder.encode(senha));
        administrador.setNivelAcesso(NivelAcesso.ADMINISTRADOR);
        administrador.setStatus(Status.ATIVO);
        repository.save(administrador);
    }
}
