package com.zheiry.barbersystem.dto.agendamento;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AgendamentoReagendarDto(

        @NotNull(message = "A nova data e hora de início são obrigatórias.")
        @Future(message = "A nova data e hora devem ser no futuro.")
        LocalDateTime novaDataHoraInicio

) {
}
