package com.zheiry.barbersystem.dto.horarioFuncionamento;

import com.zheiry.barbersystem.enums.DiaSemana;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioFuncionamentoDto(


        @NotNull(message = "Barbearia é obrigatória.")
        Long barbeariaId,

        @NotNull(message = "Dia da semana é obrigatório.")
        DiaSemana diaSemana,

        @NotNull(message = "O horário inicial de funcionamento é obrigatório.")
        LocalTime horarioInicio,

        @NotNull(message = "O horário final de funcionamento é obrigatório.")
        LocalTime horarioFim
) {
    @AssertTrue(message = "Horário de início deve ser antes do horário de fim.")
    public boolean isHorarioValido() {
        return horarioInicio == null || horarioFim == null
                || horarioInicio.isBefore(horarioFim);
    }
}

