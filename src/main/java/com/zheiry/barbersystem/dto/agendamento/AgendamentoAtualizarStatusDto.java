package com.zheiry.barbersystem.dto.agendamento;

import com.zheiry.barbersystem.enums.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

public record AgendamentoAtualizarStatusDto(

        @NotNull(message = "O novo status é obrigatório.")
        StatusAgendamento statusAgendamento

) {
}
