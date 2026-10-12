package com.zheiry.barbersystem.dto.horarioFuncionamento;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioFuncionamentoAtualizaDto(


        @NotNull(message = "O horário inicial é obrigatório.")
        LocalTime horarioInicio,

        @NotNull(message = "O horário final é obrigatório.")
        LocalTime horarioFim
) {
    @AssertTrue(message = "Horário de início deve ser antes do horário de fim.")
    public boolean isHorarioValido() {
        return horarioInicio.isBefore(horarioFim);
    }
}
