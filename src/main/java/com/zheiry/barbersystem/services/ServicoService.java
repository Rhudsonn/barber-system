package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.repositories.ServicoRepository;
import org.springframework.stereotype.Service;

@Service
public class ServicoService {

    private final ServicoRepository repository;
    public ServicoService(ServicoRepository repository) {
        this.repository = repository;
    }


    // Cadastrar Serviço - só o administrador tem altorização.
    // Listar - Ativo, Inativo.
    // Buscar por ID.
    // Buscar por nome.
    // Buscar por Categoria.
    // Atualizar informações.
    // Excluir - exclusão lógica.
    // Reativar Serviço.
}
