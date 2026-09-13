package com.zheiry.barbersystem.dto.agendamento;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoDto(

        @NotNull(message = "O ID do cliente é obrigatório.")
        Long clienteId,

        @NotNull(message = "O ID do barbeiro é obrigatório.")
        Long barbeiroId,

        @NotNull(message = "O ID do serviço é obrigatório.")
        Long servicoId,

        @NotNull(message = "A data e hora de início são obrigatórias.")
        @Future(message = "A data e hora do agendamento devem ser no futuro.")
        LocalDateTime dataHoraInicio,

        String observacao   // opcional

) {
}
