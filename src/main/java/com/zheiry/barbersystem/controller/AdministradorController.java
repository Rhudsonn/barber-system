package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.administrador.AdministradorAlteraSenhaDto;
import com.zheiry.barbersystem.dto.administrador.AdministradorAtualizaDto;
import com.zheiry.barbersystem.dto.administrador.AdministradorSaidaDto;
import com.zheiry.barbersystem.services.AdministradorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/administradores")
public class AdministradorController {

    private final AdministradorService service;

    public AdministradorController(AdministradorService service) {
        this.service = service;
    }

    @PatchMapping("/meu-perfil/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and #id == authentication.principal.id")
    public ResponseEntity<AdministradorSaidaDto> atualizarInformacoes(
            @PathVariable Long id, @RequestBody @Valid AdministradorAtualizaDto dto) {
        return ResponseEntity.ok(service.atualizarInformacoes(id, dto));
    }

    @PatchMapping("/meu-perfil/senha/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and #id == authentication.principal.id")
    public ResponseEntity<Void> alterarSenha(
            @PathVariable Long id, @RequestBody @Valid AdministradorAlteraSenhaDto dto) {
        service.alterarSenha(id, dto);
        return ResponseEntity.noContent().build();
    }
}
