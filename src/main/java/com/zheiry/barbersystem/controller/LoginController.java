package com.zheiry.barbersystem.controller;


import com.zheiry.barbersystem.config.jwt.JwtService;
import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.config.springSecurity.CustomUserDetailsService;
import com.zheiry.barbersystem.dto.LoginEntradaDto;
import com.zheiry.barbersystem.dto.LoginSaidaDto;
import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaRedefinirDto;
import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaSaidaDto;
import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaSolicitarDto;
import com.zheiry.barbersystem.dto.administrador.AdministradorSaidaDto;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroSaidaDto;
import com.zheiry.barbersystem.dto.cliente.ClienteSaidaDto;
import com.zheiry.barbersystem.entities.AdministradorEntity;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.mappers.AdministradorMapper;
import com.zheiry.barbersystem.mappers.BarbeiroMapper;
import com.zheiry.barbersystem.mappers.ClienteMapper;
import com.zheiry.barbersystem.repositories.AdministradorRepository;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.repositories.ClienteRepository;
import com.zheiry.barbersystem.services.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;
    private final AdministradorRepository administradorRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final ClienteRepository clienteRepository;

    public LoginController(AuthenticationManager authenticationManager, JwtService jwtService,
                           CustomUserDetailsService userDetailsService, RecuperacaoSenhaService recuperacaoSenhaService,
                           AdministradorRepository administradorRepository, BarbeiroRepository barbeiroRepository,
                           ClienteRepository clienteRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
        this.administradorRepository = administradorRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/meu-perfil")
    public ResponseEntity<?> meuPerfil(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new AccessDeniedException("Usuário autenticado não identificado.");
        }
        if (usuario.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            AdministradorEntity entidade = administradorRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            return ResponseEntity.ok(AdministradorMapper.toSaidaDto(entidade));
        }
        if (usuario.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_BARBEIRO"))) {
            BarbeiroEntity entidade = barbeiroRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            return ResponseEntity.ok(BarbeiroMapper.toSaidaDto(entidade));
        }
        if (usuario.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
            ClienteEntity entidade = clienteRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            return ResponseEntity.ok(ClienteMapper.toSaidaDto(entidade));
        }
        throw new AccessDeniedException("Perfil sem acesso ao próprio cadastro.");
    }

    @PostMapping
    public ResponseEntity<LoginSaidaDto> login(@RequestBody @Valid LoginEntradaDto dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.email());
        String token = jwtService.gerarToken(userDetails);

        UsuarioAutenticado usuario = (UsuarioAutenticado) userDetails;
        String perfil = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Perfil do usuário não encontrado."));
        return ResponseEntity.ok(new LoginSaidaDto(token, "Bearer", usuario.getId(), perfil));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<RecuperacaoSenhaSaidaDto> solicitarRecuperacao(
            @RequestBody @Valid RecuperacaoSenhaSolicitarDto dto) {
        return ResponseEntity.ok(recuperacaoSenhaService.solicitar(dto.email()));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody @Valid RecuperacaoSenhaRedefinirDto dto) {
        recuperacaoSenhaService.redefinir(dto);
        return ResponseEntity.noContent().build();
    }
}
