package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.services.AdministradorService;
import org.springframework.stereotype.Controller;

@Controller
public class AdministradorController {

    private final AdministradorService service;
    public AdministradorController(final AdministradorService service) {
        this.service = service;
    }




}
