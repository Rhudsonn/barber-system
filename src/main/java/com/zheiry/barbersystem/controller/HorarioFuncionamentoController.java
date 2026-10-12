package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoAtualizaDto;
import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoDto;
import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoSaidaDto;
import com.zheiry.barbersystem.enums.DiaSemana;
import com.zheiry.barbersystem.services.HorarioFuncionamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/horarios-funcionamento")
public class HorarioFuncionamentoController {

    private final HorarioFuncionamentoService service;

    public HorarioFuncionamentoController(HorarioFuncionamentoService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<HorarioFuncionamentoSaidaDto> cadastrar(
            @RequestBody @Valid HorarioFuncionamentoDto dto) {
        HorarioFuncionamentoSaidaDto saida = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<HorarioFuncionamentoSaidaDto> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid HorarioFuncionamentoAtualizaDto dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/barbearia/{barbeariaId}")
    public ResponseEntity<List<HorarioFuncionamentoSaidaDto>> listarPorBarbearia(
            @PathVariable Long barbeariaId) {
        return ResponseEntity.ok(service.listarPorBarbearia(barbeariaId));
    }

    @GetMapping("/barbearia/{barbeariaId}/dia/{diaSemana}")
    public ResponseEntity<List<HorarioFuncionamentoSaidaDto>> listarPorBarbeariaEDia(
            @PathVariable Long barbeariaId,
            @PathVariable DiaSemana diaSemana) {
        return ResponseEntity.ok(service.listarPorBarbeariaEDia(barbeariaId, diaSemana));
    }
}
