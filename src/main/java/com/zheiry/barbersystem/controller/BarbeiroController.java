package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroAlteraSenhaDto;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroAtualizaDto;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroDto;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroSaidaDto;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.services.BarbeiroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbeiros")
public class BarbeiroController {

    private final BarbeiroService service;
    public BarbeiroController(BarbeiroService service) {
        this.service = service;
    }


    // Cadastrar barbeiro - sómente administrador tem altorização.
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BarbeiroSaidaDto> cadastrar(@RequestBody @Valid BarbeiroDto dto) {
        BarbeiroSaidaDto saida = service.cadastroBarbeiro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }


    // Lista de barbeiros ativo
    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('CLIENTE','BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<List<?>> listarAtivos(Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (isAdmin){
            return ResponseEntity.status(HttpStatus.OK).body(service.listaPorStatusAdmin(Status.ATIVO));
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.listarAtivos());
    }

    // Lista Inativos
    @GetMapping("/inativos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<BarbeiroSaidaDto>> listarInativos(){
        return ResponseEntity.status(HttpStatus.OK).body(service.listaPorStatusAdmin(Status.INATIVO));
    }



    // Buscar barbeiro por nome.
    @GetMapping("/busca/nome")
    public ResponseEntity<List<?>> buscarPorNome(@RequestParam String nome){
        return ResponseEntity.status(HttpStatus.OK).body(service.buscarPorNome(nome));
    }


    @PatchMapping("/meu-perfil/{id}")
    @PreAuthorize("hasRole('BARBEIRO') and #id == authentication.principal.id")
    public ResponseEntity<BarbeiroSaidaDto> atualizarInformacoes(@PathVariable Long id, @RequestBody @Valid BarbeiroAtualizaDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(service.atualizarInformacoes(id, dto));
    }


    // Alterar senha - somente o barbeiro tem altorização
    @PatchMapping("/{id}/senha")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> alterarSenha(@PathVariable Long id, @RequestBody @Valid BarbeiroAlteraSenhaDto dto) {
        service.alterarSenha(id, dto);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    // Desativar barbeiro exclusao lógica - somente administrador tem altorização
    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desativarBarbeiro(@PathVariable Long id){
        service.desativarBarbeiro(id);
        return ResponseEntity.noContent().build();  //HTTP 204
    }

    // Reativar barbeiro - sómente administrador tem altorização.
    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> reativarBarbeiro(@PathVariable Long id){
        service.reativarBarbeiro(id);
        return ResponseEntity.noContent().build();  //HTTP 204
    }






}
