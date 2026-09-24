package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoDto;
import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoSaidaDto;
import com.zheiry.barbersystem.services.HorarioDeTrabalhoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/horarios-trabalho")
public class HorarioDeTrabalhoController {

    private final HorarioDeTrabalhoService service;
    public HorarioDeTrabalhoController(HorarioDeTrabalhoService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<HorarioDeTrabalhoSaidaDto> cadastrar(@RequestBody @Valid HorarioDeTrabalhoDto dto){
        HorarioDeTrabalhoSaidaDto saida = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }
}
