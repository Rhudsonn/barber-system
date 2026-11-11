package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.administrador.AdministradorAtualizaDto;
import com.zheiry.barbersystem.dto.administrador.AdministradorSaidaDto;
import com.zheiry.barbersystem.entities.AdministradorEntity;

public final class AdministradorMapper {

    private AdministradorMapper() {}

    public static AdministradorSaidaDto toSaidaDto(AdministradorEntity entity) {
        if (entity == null) return null;
        return new AdministradorSaidaDto(entity.getId(), entity.getNome(), entity.getTelefone(), entity.getEmail());
    }

    public static void atualizarEntity(AdministradorAtualizaDto dto, AdministradorEntity entity) {
        if (dto == null || entity == null) return;
        if (dto.nome() != null) entity.setNome(dto.nome());
        if (dto.telefone() != null) entity.setTelefone(dto.telefone());
        if (dto.email() != null) entity.setEmail(dto.email());
    }
}
