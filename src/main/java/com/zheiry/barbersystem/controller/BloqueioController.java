package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.bloqueio.BloqueioDto;
import com.zheiry.barbersystem.dto.bloqueio.BloqueioSaidaDto;
import com.zheiry.barbersystem.services.BloqueioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bloqueio")
public class BloqueioController {

    private final BloqueioService bloqueioService;
    public BloqueioController(BloqueioService bloqueioService) {
        this.bloqueioService = bloqueioService;
    }

    // Criar
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BloqueioSaidaDto> cadastrar(@RequestBody @Valid BloqueioDto dto){
        BloqueioSaidaDto saida = bloqueioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }

    // Atualizar
    // Excluir
    // buscarPorId
    // listarPorBarbeiro
    // listarPorBarbeiroEPeriodo
    // existeBloqueioNoPeriodo




}
