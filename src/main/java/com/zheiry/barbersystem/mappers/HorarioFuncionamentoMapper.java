package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoAtualizaDto;
import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoDto;
import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoSaidaDto;
import com.zheiry.barbersystem.entities.BarbeariaEntity;
import com.zheiry.barbersystem.entities.HorarioFuncionamentoEntity;

import java.util.List;

public class HorarioFuncionamentoMapper {

    public HorarioFuncionamentoMapper() {
    }

    // DTO → Entity
    public static HorarioFuncionamentoEntity toEntity(HorarioFuncionamentoDto dto, BarbeariaEntity barbearia) {
        if (dto == null) return null;

        HorarioFuncionamentoEntity entity = new HorarioFuncionamentoEntity();
        entity.setBarbearia(barbearia);
        entity.setDiaSemana(dto.diaSemana());
        entity.setHorarioInicio(dto.horarioInicio());
        entity.setHorarioFim(dto.horarioFim());
        return entity;
    }

    // Entity → SaidaDto
    public static HorarioFuncionamentoSaidaDto toSaidaDto(HorarioFuncionamentoEntity entity) {
        if (entity == null) return null;

        return new HorarioFuncionamentoSaidaDto(
                entity.getId(),
                entity.getBarbearia().getId(),
                entity.getBarbearia().getNomeFantasia(),
                entity.getDiaSemana(),
                entity.getHorarioInicio(),
                entity.getHorarioFim()
        );
    }

    public static List<HorarioFuncionamentoSaidaDto> toSaidaDtoList(List<HorarioFuncionamentoEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(HorarioFuncionamentoMapper::toSaidaDto)
                .toList();
    }

    // Aplica dados do DTO de atualização na Entity
    public static void atualizarEntity(HorarioFuncionamentoAtualizaDto dto, HorarioFuncionamentoEntity entity) {
        if (dto == null || entity == null) return;

        entity.setHorarioInicio(dto.horarioInicio());
        entity.setHorarioFim(dto.horarioFim());
    }
}
