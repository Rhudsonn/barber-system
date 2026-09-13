package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.repositories.AgendamentoRepository;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repository;
    public AgendamentoService(AgendamentoRepository repository) {
        this.repository = repository;
    }



}
