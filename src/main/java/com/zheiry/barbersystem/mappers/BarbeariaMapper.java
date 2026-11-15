package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.barbearia.BarbeariaAtualizaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaPublicaDto;
import com.zheiry.barbersystem.dto.barbearia.BarbeariaSaidaDto;
import com.zheiry.barbersystem.entities.BarbeariaEntity;
import com.zheiry.barbersystem.util.TelefoneWhatsApp;

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
        entity.setWhatsapp(TelefoneWhatsApp.normalizar(dto.whatsapp()));
        entity.setInstagram(dto.instagram());
        entity.setEmail(dto.email());
        entity.setEndereco(dto.endereco());
        entity.setCidade(dto.cidade());
        entity.setEstado(dto.estado() != null ? dto.estado().toUpperCase() : null);
        entity.setCep(dto.cep());
        entity.setDescricao(dto.descricao());
        return entity;
    }

    public static BarbeariaPublicaDto toPublicaDto(BarbeariaEntity entity) {
        if (entity == null) return null;
        return new BarbeariaPublicaDto(entity.getId(), entity.getNomeFantasia(), entity.getLogoUrl(),
                entity.getDescricao(), entity.getEndereco(), entity.getCidade(), entity.getWhatsapp());
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
                entity.getInstagram(),
                entity.getEmail(),
                entity.getEndereco(),
                entity.getCidade(),
                entity.getEstado(),
                entity.getCep(),
                entity.getDescricao(),
                entity.getLogoUrl(),
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
                entity.getInstagram(),
                entity.getEmail(),
                entity.getEndereco(),
                entity.getCidade(),
                entity.getEstado(),
                entity.getCep(),
                entity.getDescricao(),
                entity.getLogoUrl(),
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

        if (dto.nomeFantasia() != null) entity.setNomeFantasia(dto.nomeFantasia());
        if (dto.razaoSocial() != null) entity.setRazaoSocial(dto.razaoSocial());
        if (dto.telefone() != null) entity.setTelefone(dto.telefone());
        if (dto.whatsapp() != null) entity.setWhatsapp(TelefoneWhatsApp.normalizar(dto.whatsapp()));
        if (dto.instagram() != null) entity.setInstagram(dto.instagram());
        if (dto.email() != null) entity.setEmail(dto.email());
        if (dto.endereco() != null) entity.setEndereco(dto.endereco());
        if (dto.cidade() != null) entity.setCidade(dto.cidade());
        if (dto.estado() != null) entity.setEstado(dto.estado().toUpperCase());
        if (dto.cep() != null) entity.setCep(dto.cep());
        if (dto.descricao() != null) entity.setDescricao(dto.descricao());
        if (dto.logoUrl() != null) {
            entity.setLogoUrl(dto.logoUrl());
        }
    }





}
