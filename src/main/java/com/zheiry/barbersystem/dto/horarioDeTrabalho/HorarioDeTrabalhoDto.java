package com.zheiry.barbersystem.dto.horarioDeTrabalho;

import com.zheiry.barbersystem.enums.DiaSemana;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioDeTrabalhoDto(

        @NotNull(message = "Barbeiro é obrigatório.")
        Long barbeiroId,

        @NotNull(message = "Dia da semana é obrigatório.")
        DiaSemana diaSemana,

        @NotNull(message = "O horário Inicial da jornada é obrigatório")
        LocalTime horarioInicio,

        @NotNull(message = "O horário Final do expediente é obrigatório")
        LocalTime horarioFim


) {
    // Metodo que valida horário de início e horário fim da jornada.
    @AssertTrue(message = "Horário de início deve ser antes do horário de fim")
    public boolean isHorarioValido() {
        return horarioInicio == null || horarioFim == null
                || horarioInicio.isBefore(horarioFim);
    }
}
