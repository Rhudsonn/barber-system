package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.bloqueio.BloqueioAtualizaDto;
import com.zheiry.barbersystem.dto.bloqueio.BloqueioDto;
import com.zheiry.barbersystem.dto.bloqueio.BloqueioSaidaDto;
import com.zheiry.barbersystem.services.BloqueioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bloqueio")
public class BloqueioController {

    private final BloqueioService bloqueioService;
    public BloqueioController(BloqueioService bloqueioService) {
        this.bloqueioService = bloqueioService;
    }

    // Criar — somente administrador
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BloqueioSaidaDto> cadastrar(@RequestBody @Valid BloqueioDto dto){
        BloqueioSaidaDto saida = bloqueioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }

    // Atualizar — somente administrador
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BloqueioSaidaDto> atualizar(@PathVariable Long id,
                                                      @RequestBody @Valid BloqueioAtualizaDto dto) {
        return ResponseEntity.ok(bloqueioService.atualizar(id, dto));
    }

    // Excluir — somente administrador
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        bloqueioService.excluir(id);
    }

    // Buscar por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BARBEIRO')")
    public ResponseEntity<BloqueioSaidaDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bloqueioService.buscarPorId(id));
    }

    // Listar todos os bloqueios de um barbeiro
    @GetMapping("/barbeiro/{barbeiroId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BARBEIRO')")
    public ResponseEntity<List<BloqueioSaidaDto>> listarPorBarbeiro(@PathVariable Long barbeiroId) {
        return ResponseEntity.ok(bloqueioService.listarPorBarbeiro(barbeiroId));
    }

    // Listar bloqueios de um barbeiro em um intervalo (para agenda)
    // Ex.: GET /bloqueio/barbeiro/1/periodo?inicio=2026-10-01T00:00:00&fim=2026-10-31T23:59:59
    @GetMapping("/barbeiro/{barbeiroId}/periodo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BARBEIRO')")
    public ResponseEntity<List<BloqueioSaidaDto>> listarPorBarbeiroEPeriodo(
            @PathVariable Long barbeiroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(bloqueioService.listarPorBarbeiroEPeriodo(barbeiroId, inicio, fim));
    }




}
