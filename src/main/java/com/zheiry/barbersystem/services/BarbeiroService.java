package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import org.springframework.stereotype.Service;

@Service
public class BarbeiroService {

    private final BarbeiroRepository repository;

    public BarbeiroService(BarbeiroRepository repository) {
        this.repository = repository;
    }


}
