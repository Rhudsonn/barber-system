package com.zheiry.barbersystem.controller.barbeiro;

import com.zheiry.barbersystem.services.BarbeiroService;
import org.springframework.stereotype.Controller;

@Controller
public class BarbeiroControllerPage {

    private final BarbeiroService service;
    public BarbeiroControllerPage(BarbeiroService service) {
        this.service = service;
    }

}
