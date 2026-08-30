package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.repositories.AdministradorRepository;
import org.springframework.stereotype.Service;

@Service
public class AdministradorService {

    private final  AdministradorRepository repository;
    public AdministradorService(final AdministradorRepository repository) {
        this.repository = repository;
    }

}
