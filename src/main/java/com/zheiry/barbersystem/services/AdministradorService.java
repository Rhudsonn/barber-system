package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.dto.administrador.AdministradorAlteraSenhaDto;
import com.zheiry.barbersystem.dto.administrador.AdministradorAtualizaDto;
import com.zheiry.barbersystem.dto.administrador.AdministradorSaidaDto;
import com.zheiry.barbersystem.entities.AdministradorEntity;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.DuplicateResourceException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.AdministradorMapper;
import com.zheiry.barbersystem.repositories.AdministradorRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdministradorService {

    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorService(AdministradorRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdministradorSaidaDto atualizarInformacoes(Long id, AdministradorAtualizaDto dto) {
        AdministradorEntity administrador = buscarDoUsuarioAutenticado(id);

        if (dto.email() != null && repository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new DuplicateResourceException("O e-mail informado já está sendo utilizado por outro usuário.");
        }
        if (dto.telefone() != null && repository.existsByTelefoneAndIdNot(dto.telefone(), id)) {
            throw new DuplicateResourceException("O telefone informado já está sendo utilizado por outro usuário.");
        }

        AdministradorMapper.atualizarEntity(dto, administrador);
        return AdministradorMapper.toSaidaDto(repository.save(administrador));
    }

    @Transactional
    public void alterarSenha(Long id, AdministradorAlteraSenhaDto dto) {
        AdministradorEntity administrador = buscarDoUsuarioAutenticado(id);

        if (!passwordEncoder.matches(dto.senhaAtual(), administrador.getSenha())) {
            throw new BadRequestException("Senha atual incorreta!");
        }
        if (!dto.novaSenha().equals(dto.confirmaNovaSenha())) {
            throw new BadRequestException("Nova senha e confirmação não conferem!");
        }
        if (passwordEncoder.matches(dto.novaSenha(), administrador.getSenha())) {
            throw new BadRequestException("A nova senha deve ser diferente da senha atual!");
        }

        administrador.setSenha(passwordEncoder.encode(dto.novaSenha()));
        repository.save(administrador);
    }

    private AdministradorEntity buscarDoUsuarioAutenticado(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"))
                || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)
                || !usuario.getId().equals(id)) {
            throw new AccessDeniedException("Apenas o próprio administrador pode alterar seus dados.");
        }
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador não encontrado."));
    }
}
