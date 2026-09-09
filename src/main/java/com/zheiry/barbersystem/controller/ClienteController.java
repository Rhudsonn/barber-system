package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.cliente.ClienteDto;
import com.zheiry.barbersystem.dto.cliente.ClienteSaidaDto;
import com.zheiry.barbersystem.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



}
