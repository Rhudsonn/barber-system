package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoDto;
import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoSaidaDto;
import com.zheiry.barbersystem.entities.HorarioDeTrabalhoEntity;

import java.util.List;

public class HorarioDeTrabalhoMapper {

    public HorarioDeTrabalhoMapper() {
    }


    // Dto - Entity (toEntity)
    public static HorarioDeTrabalhoEntity toEntity(HorarioDeTrabalhoDto dto) {
        if (dto == null) return null;
        HorarioDeTrabalhoEntity entity = new HorarioDeTrabalhoEntity();
        entity.setDiaSemana(dto.diaSemana());
        entity.setHorarioInicio(dto.horarioInicio());
        entity.setHorarioFim(dto.horarioFim());
        return entity;
    }


    // Entity - Dto (toSaidaDto)
    public static HorarioDeTrabalhoSaidaDto toSaidaDto(HorarioDeTrabalhoEntity entity) {
        if (entity == null) return null;
        return new HorarioDeTrabalhoSaidaDto(
                entity.getId(),
                entity.getBarbeiro().getId(),
                entity.getBarbeiro().getNome(),
                entity.getDiaSemana(),
                entity.getHorarioInicio(),
                entity.getHorarioFim()
        );
    }

    //Converte uma lista de Entities em uma lista de DTOs de saída. (toSaidaDtoList)
    public static List<HorarioDeTrabalhoSaidaDto> toSaidaDtoList(List<HorarioDeTrabalhoEntity> entity){
        if (entity == null) return List.of();
        return entity.stream()
                .map(HorarioDeTrabalhoMapper::toSaidaDto)
                .toList();
    }


    // //Aplica os dados do DTO de atualização em cima de uma Entity (atualizarEntit)

}
