package com.zheiry.barbersystem.controller.barbeiro;

import com.zheiry.barbersystem.services.BarbeiroService;
import org.springframework.stereotype.Controller;

@Controller
public class BarbeiroController {

    private final BarbeiroService service;
    public BarbeiroController(BarbeiroService service) {
        this.service = service;
    }

}
