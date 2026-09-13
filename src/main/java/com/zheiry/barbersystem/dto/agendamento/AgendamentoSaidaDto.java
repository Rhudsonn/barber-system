package com.zheiry.barbersystem.dto.agendamento;

import com.zheiry.barbersystem.enums.StatusAgendamento;

import java.time.LocalDateTime;

public record AgendamentoSaidaDto(

        Long id,

        // Dados resumidos do Cliente
        Long clienteId,
        String clienteNome,

        // Dados resumidos do Barbeiro
        Long barbeiroId,
        String barbeiroNome,

        // Dados resumidos do Serviço
        Long servicoId,
        String servicoNome,
        Integer servicoDuracaoMinutos,
        Double servicoPreco,

        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,

        StatusAgendamento statusAgendamento,

        String observacao,

        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm

) {
}
