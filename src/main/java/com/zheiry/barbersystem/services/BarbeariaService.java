package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.barbearia.BarbeariaAtualizaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaSaidaDto;
import com.zheiry.barbersystem.entities.BarbeariaEntity;
import com.zheiry.barbersystem.exception.DuplicateResourceException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.BarbeariaMapper;
import com.zheiry.barbersystem.repositories.BarbeariaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarbeariaService {


    private final BarbeariaRepository repository;

    public BarbeariaService(BarbeariaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BarbeariaSaidaDto cadastrar(BarbeariaDto dto) {
        if (repository.existsByCnpj(dto.cnpj())) {
            throw new DuplicateResourceException("CNPJ já está cadastrado.");
        }
        if (repository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("E-mail já está em uso.");
        }

        BarbeariaEntity entity = BarbeariaMapper.toEntity(dto);
        BarbeariaEntity salva = repository.save(entity);
        return BarbeariaMapper.toSaidaDtoSemHorarios(salva);
    }

    @Transactional
    public BarbeariaSaidaDto buscarPorId(Long id) {
        BarbeariaEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada."));
        entity.getHorarios().size();
        return BarbeariaMapper.toSaidaDto(entity);
    }

    @Transactional
    public BarbeariaSaidaDto buscarPrincipal() {
        BarbeariaEntity entity = repository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma barbearia cadastrada."));
        entity.getHorarios().size();
        return BarbeariaMapper.toSaidaDto(entity);
    }

    @Transactional
    public List<BarbeariaSaidaDto> listarTodas() {
        return BarbeariaMapper.toSaidaDtoList(repository.findAll());
    }

    @Transactional
    public BarbeariaSaidaDto atualizar(Long id, BarbeariaAtualizaDto dto) {
        BarbeariaEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada."));

        if (repository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new DuplicateResourceException("E-mail já está em uso por outra barbearia.");
        }

        BarbeariaMapper.atualizarEntity(dto, entity);
        BarbeariaEntity atualizada = repository.save(entity);
        return BarbeariaMapper.toSaidaDtoSemHorarios(atualizada);
    }
}
