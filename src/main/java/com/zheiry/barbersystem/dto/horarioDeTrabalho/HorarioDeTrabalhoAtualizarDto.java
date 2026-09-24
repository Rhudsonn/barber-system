package com.zheiry.barbersystem.dto.horarioDeTrabalho;



import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioDeTrabalhoAtualizarDto(

        // DiaSemana diaSemana, ------> Se quiser mudar o dia da semana, o usuário deve excluir e criar um novo.
        @NotNull(message = "O horário inicial é obrigatório")
        LocalTime horarioInicio,

        @NotNull(message = "O horário final é obrigatório")
        LocalTime horarioFim

) {
    @AssertTrue(message = "Horário de início deve ser antes do horário de fim")
    public boolean isHorarioValido() {
        return horarioInicio.isBefore(horarioFim);
    }
}
