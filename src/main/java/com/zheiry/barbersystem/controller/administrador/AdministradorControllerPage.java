package com.zheiry.barbersystem.controller.administrador;

import com.zheiry.barbersystem.services.AdministradorService;
import org.springframework.stereotype.Controller;

@Controller
public class AdministradorControllerPage {

    private final AdministradorService service;
    public AdministradorControllerPage(final AdministradorService service) {
        this.service = service;}




}
