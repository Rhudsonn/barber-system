package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.agendamento.AgendamentoDto;
import com.zheiry.barbersystem.dto.agendamento.AgendamentoSaidaDto;
import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.entities.ServicoEntity;
import com.zheiry.barbersystem.enums.StatusAgendamento;

import java.util.List;

public class AgendamentoMapper {

    // Converte Entity → DTO de saída
    public static AgendamentoSaidaDto toSaidaDto(AgendamentoEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AgendamentoSaidaDto(
                entity.getId(),

                // Cliente
                entity.getCliente() != null ? entity.getCliente().getId() : null,
                entity.getCliente() != null ? entity.getCliente().getNome() : null,

                // Barbeiro
                entity.getBarbeiro() != null ? entity.getBarbeiro().getId() : null,
                entity.getBarbeiro() != null ? entity.getBarbeiro().getNome() : null,

                // Serviço
                entity.getServico() != null ? entity.getServico().getId() : null,
                entity.getServico() != null ? entity.getServico().getNome() : null,
                entity.getServico() != null ? entity.getServico().getDuracaoEmMinutos() : null,
                entity.getServico() != null ? entity.getServico().getPreco() : null,

                entity.getDataHoraInicio(),
                entity.getDataHoraFim(),
                entity.getStatusAgendamento(),
                entity.getObservacao(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    // Converte lista de Entities → lista de DTOs de saída
    public static List<AgendamentoSaidaDto> toSaidaDtoList(List<AgendamentoEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(AgendamentoMapper::toSaidaDto)
                .toList();
    }

    /**
     * Cria uma Entity a partir do DTO de entrada + entidades já buscadas no Service.
     * O Service é responsável por buscar Cliente, Barbeiro e Serviço antes de chamar este método.
     */
    public static AgendamentoEntity toEntity(AgendamentoDto dto,
                                             ClienteEntity cliente,
                                             BarbeiroEntity barbeiro,
                                             ServicoEntity servico) {

        if (dto == null) {
            return null;
        }

        AgendamentoEntity entity = new AgendamentoEntity();

        entity.setCliente(cliente);
        entity.setBarbeiro(barbeiro);
        entity.setServico(servico);
        entity.setDataHoraInicio(dto.dataHoraInicio());

        // Calcula a data/hora de fim com base na duração do serviço
        if (servico != null && servico.getDuracaoEmMinutos() != null) {
            entity.setDataHoraFim(dto.dataHoraInicio().plusMinutes(servico.getDuracaoEmMinutos()));
        }

        entity.setObservacao(dto.observacao());
        entity.setStatusAgendamento(StatusAgendamento.AGENDADO); // status inicial

        return entity;
    }
}
