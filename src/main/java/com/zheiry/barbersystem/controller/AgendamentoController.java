package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.services.AgendamentoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agendamento")
public class AgendamentoController {

    private final AgendamentoService service;
    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }



}
