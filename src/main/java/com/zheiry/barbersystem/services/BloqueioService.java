package com.zheiry.barbersystem.services;


import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.repositories.BloqueioRepository;
import org.springframework.stereotype.Service;

@Service
public class BloqueioService {

    private final BloqueioRepository repository;
    private final BarbeiroRepository barbeiroRepository;

    public BloqueioService(BloqueioRepository repository, BarbeiroRepository barbeiroRepository) {
        this.repository = repository;
        this.barbeiroRepository = barbeiroRepository;
    }


    // Criar
    // Atualizar
    // Excluir
    // buscarPorId
    // listarPorBarbeiro
    // listarPorBarbeiroEPeriodo
    // existeBloqueioNoPeriodo

}
