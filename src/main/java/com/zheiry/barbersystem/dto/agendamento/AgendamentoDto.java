package com.zheiry.barbersystem.dto.agendamento;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoDto(

        Long clienteId,

        String clienteAvulsoNome,

        String clienteAvulsoTelefone,

        @NotNull(message = "O ID do barbeiro é obrigatório.")
        Long barbeiroId,

        @NotNull(message = "O ID do serviço é obrigatório.")
        Long servicoId,

        @NotNull(message = "A data e hora de início são obrigatórias.")
        @Future(message = "A data e hora do agendamento devem ser no futuro.")
        LocalDateTime dataHoraInicio,

        String observacao   // opcional

) {
    public AgendamentoDto(Long clienteId, Long barbeiroId, Long servicoId, LocalDateTime dataHoraInicio, String observacao) {
        this(clienteId, null, null, barbeiroId, servicoId, dataHoraInicio, observacao);
    }
}
