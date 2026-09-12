package com.zheiry.barbersystem.controller;


import com.zheiry.barbersystem.services.ServicoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService service;
    public ServicoController(ServicoService service) {
        this.service = service;
    }







}
