package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.barbearia.BarbeariaAtualizaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaSaidaDto;
import com.zheiry.barbersystem.services.BarbeariaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbearias")
public class BarbeariaController {

    private final BarbeariaService service;

    public BarbeariaController(BarbeariaService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BarbeariaSaidaDto> cadastrar(@RequestBody @Valid BarbeariaDto dto) {
        BarbeariaSaidaDto saida = service.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarbeariaSaidaDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/principal")
    public ResponseEntity<BarbeariaSaidaDto> buscarPrincipal() {
        return ResponseEntity.ok(service.buscarPrincipal());
    }

    @GetMapping
    public ResponseEntity<List<BarbeariaSaidaDto>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BarbeariaSaidaDto> atualizar(@PathVariable Long id,
                                                       @RequestBody @Valid BarbeariaAtualizaDto dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }



}
