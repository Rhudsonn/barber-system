package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.cliente.ClienteDto;
import com.zheiry.barbersystem.dto.cliente.ClienteSaidaDto;
import com.zheiry.barbersystem.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;
    public ClienteController(ClienteService service) {this.service = service;}

    // Cadastro de cliente - cliente se cadastra - publico (opção de cadastro, ficara na tela de login).
    @PostMapping
    public ResponseEntity<ClienteSaidaDto> cadastrarCliente(@RequestBody @Valid ClienteDto dto){
        ClienteSaidaDto saida = service.cadastroCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }


    //Listar clientes com status ativo.
    @GetMapping("lista/ativo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ClienteSaidaDto>> listaClientesStatusAtivo(){
        return ResponseEntity.ok(service.listaClientesStatusAtivo());
    }

    //Lista clientes com status inativo.
    @GetMapping("lista/inativo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ClienteSaidaDto>> listaClientesStatusInativo(){
        return ResponseEntity.ok(service.listaClientesStatusInativo());
    }




}
