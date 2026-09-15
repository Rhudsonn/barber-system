package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.agendamento.AgendamentoDto;
import com.zheiry.barbersystem.dto.agendamento.AgendamentoSaidaDto;
import com.zheiry.barbersystem.services.AgendamentoService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agendamento")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    // Criar Agendamento
    // Cliente, Barbeiro e Admin podem criar (você pode restringir depois se quiser).
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> criarAgendamento(@RequestBody @Valid AgendamentoDto dto) throws BadRequestException {
        AgendamentoSaidaDto saida = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }



}
