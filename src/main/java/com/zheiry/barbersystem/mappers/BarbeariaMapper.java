package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.barbearia.BarbeariaAtualizaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaSaidaDto;
import com.zheiry.barbersystem.entities.BarbeariaEntity;

import java.util.List;

public class BarbeariaMapper {

    public BarbeariaMapper() {
    }

    // DTO → Entity
    public static BarbeariaEntity toEntity(BarbeariaDto dto) {
        if (dto == null) return null;

        BarbeariaEntity entity = new BarbeariaEntity();
        entity.setNomeFantasia(dto.nomeFantasia());
        entity.setRazaoSocial(dto.razaoSocial());
        entity.setCnpj(dto.cnpj());
        entity.setTelefone(dto.telefone());
        entity.setWhatsapp(dto.whatsapp());
        entity.setEmail(dto.email());
        entity.setEndereco(dto.endereco());
        entity.setCidade(dto.cidade());
        entity.setEstado(dto.estado() != null ? dto.estado().toUpperCase() : null);
        entity.setCep(dto.cep());
        entity.setDescricao(dto.descricao());
        return entity;
    }

    // Entity → SaidaDto (com horários)
    public static BarbeariaSaidaDto toSaidaDto(BarbeariaEntity entity) {
        if (entity == null) return null;

        return new BarbeariaSaidaDto(
                entity.getId(),
                entity.getNomeFantasia(),
                entity.getRazaoSocial(),
                entity.getCnpj(),
                entity.getTelefone(),
                entity.getWhatsapp(),
                entity.getEmail(),
                entity.getEndereco(),
                entity.getCidade(),
                entity.getEstado(),
                entity.getCep(),
                entity.getDescricao(),
                HorarioFuncionamentoMapper.toSaidaDtoList(entity.getHorarios())
        );
    }

    // Entity → SaidaDto sem carregar horários (evita LazyInitialization em listagens simples)
    public static BarbeariaSaidaDto toSaidaDtoSemHorarios(BarbeariaEntity entity) {
        if (entity == null) return null;

        return new BarbeariaSaidaDto(
                entity.getId(),
                entity.getNomeFantasia(),
                entity.getRazaoSocial(),
                entity.getCnpj(),
                entity.getTelefone(),
                entity.getWhatsapp(),
                entity.getEmail(),
                entity.getEndereco(),
                entity.getCidade(),
                entity.getEstado(),
                entity.getCep(),
                entity.getDescricao(),
                List.of()
        );
    }

    public static List<BarbeariaSaidaDto> toSaidaDtoList(List<BarbeariaEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(BarbeariaMapper::toSaidaDtoSemHorarios)
                .toList();
    }

    // Aplica dados do DTO de atualização na Entity (CNPJ não muda)
    public static void atualizarEntity(BarbeariaAtualizaDto dto, BarbeariaEntity entity) {
        if (dto == null || entity == null) return;

        entity.setNomeFantasia(dto.nomeFantasia());
        entity.setRazaoSocial(dto.razaoSocial());
        entity.setTelefone(dto.telefone());
        entity.setWhatsapp(dto.whatsapp());
        entity.setEmail(dto.email());
        entity.setEndereco(dto.endereco());
        entity.setCidade(dto.cidade());
        entity.setEstado(dto.estado() != null ? dto.estado().toUpperCase() : null);
        entity.setCep(dto.cep());
        entity.setDescricao(dto.descricao());
    }





}
