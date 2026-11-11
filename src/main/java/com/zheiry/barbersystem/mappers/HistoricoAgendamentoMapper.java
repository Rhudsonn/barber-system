package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.agendamento.HistoricoAgendamentoDto;
import com.zheiry.barbersystem.entities.HistoricoAgendamentoEntity;

import java.util.List;

public final class HistoricoAgendamentoMapper {
    private HistoricoAgendamentoMapper() { }

    public static HistoricoAgendamentoDto paraSaidaDto(HistoricoAgendamentoEntity entity) {
        return new HistoricoAgendamentoDto(entity.getId(), entity.getTipoEvento(), entity.getRegistradoEm(),
                entity.getUsuarioId(), entity.getUsuarioEmail(), entity.getPerfilUsuario(),
                entity.getClienteId(), entity.getClienteNome(), entity.getBarbeiroId(), entity.getBarbeiroNome(),
                entity.getServicoId(), entity.getServicoNome(), entity.getValorServico(),
                entity.getServicoAdicionalId(), entity.getServicoAdicionalNome(), entity.getValorServicoAdicional(),
                entity.getDataHoraAnterior(), entity.getDataHoraNova(), entity.getFormaPagamento(),
                entity.getValorTotalComanda());
    }

    public static List<HistoricoAgendamentoDto> paraSaidaDtoList(List<HistoricoAgendamentoEntity> entities) {
        return entities.stream().map(HistoricoAgendamentoMapper::paraSaidaDto).toList();
    }
}
