package com.zheiry.barbersystem.controller.barbeiro;

import com.zheiry.barbersystem.dto.barbeiro.BarbeiroDto;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroSaidaDto;
import com.zheiry.barbersystem.services.BarbeiroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbeiros")
public class BarbeiroController {

    private final BarbeiroService service;
    public BarbeiroController(BarbeiroService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<BarbeiroSaidaDto> cadastrar(@RequestBody @Valid BarbeiroDto dto) {
        BarbeiroSaidaDto saida = service.cadastroBarbeiro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }


    @GetMapping("/lista/ativos")
    public ResponseEntity<List<BarbeiroSaidaDto>> listaBarbeirosAtivos(){
        return ResponseEntity.ok(service.listaBarbeirosAtivos());
    }

    @GetMapping("/lista/inativos")
    public ResponseEntity<List<BarbeiroSaidaDto>> listaBarbeirosInativos(){
        return ResponseEntity.ok(service.listaBarbeirosInativos());
    }




}
