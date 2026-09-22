package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.repositories.HorarioDeTrabalhoRepository;
import org.springframework.stereotype.Service;

@Service
public class HorarioDeTrabalhoService {

    private final HorarioDeTrabalhoRepository repository;
    public HorarioDeTrabalhoService(HorarioDeTrabalhoRepository repository) {
        this.repository = repository;
    }


}
