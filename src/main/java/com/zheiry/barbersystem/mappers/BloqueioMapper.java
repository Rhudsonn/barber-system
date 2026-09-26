package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.bloqueio.BloqueioAtualizaDto;
import com.zheiry.barbersystem.dto.bloqueio.BloqueioDto;
import com.zheiry.barbersystem.dto.bloqueio.BloqueioSaidaDto;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.BloqueioEntity;

import java.util.List;

public class BloqueioMapper {

    private BloqueioMapper() {}

    // Dto - Entity
    // Converte os dados recebidos do formulário (BloqueioDto) para um objeto de banco de dados (BloqueioEntity).
    public static BloqueioEntity toEntity(BloqueioDto dto, BarbeiroEntity barbeiro){

        if (dto == null) return null;

        BloqueioEntity entity = new BloqueioEntity();

        entity.setBarbeiro(barbeiro);
        entity.setDataInicio(dto.dataInicio());
        entity.setDataFim(dto.dataFim());
        entity.setMotivo(dto.motivo());

        return entity;
    }


    // Entity - Dto
    // Converte a Entidade vinda do banco de dados (BloqueioEntity) para o DTO (BloqueioSaidaDto)
    public static BloqueioSaidaDto toSaidaDto(BloqueioEntity entity){

        if (entity == null) return null;

        return new BloqueioSaidaDto(
                entity.getId(),
                entity.getBarbeiro().getId(),
                entity.getBarbeiro().getNome(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getMotivo()
        );
    }


    //Converte uma lista de Entities em uma lista de DTOs de saída.
    //Reaproveita o método individual acima, evitando duplicação
    public static List<BloqueioSaidaDto> toSaidaDtoList(List<BloqueioEntity> entities){

        // Proteção contra lista nula: em vez de propagar o "null" (o que obrigaria
        // todo codigo que chama esse metodo a fazer uma checagem extra antes de
        // iterar sobre o resultado), retornamos uma lista vazia e imutável.
        // Isso segue a boa prática de "nunca retornar null para coleções",
        // deixando o contrato do metodo mais previsível e seguro de usar.
        if (entities == null){
            return List.of();
        }

        return entities.stream()
                .map(BloqueioMapper::toSaidaDto)
                .toList();
    }


    // Aplica os dados do DTO de atualização em cima de uma Entity (atualizarEntit)
    public static void atualizarEntity(BloqueioAtualizaDto dto, BloqueioEntity entity){

        if (dto == null || entity == null) return;

        entity.setDataInicio(dto.dataInicio());
        entity.setDataFim(dto.dataFim());
        entity.setMotivo(dto.motivo());
    }

}
