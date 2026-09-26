package com.zheiry.barbersystem.dto.bloqueio;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BloqueioDto(

        @NotNull(message = "O ID do barbeiro é obrigatório.")
        Long barbeiroId,

        @NotNull(message = "A data do bloqueio é obrigatória.")
        @Future(message = "A data do bloqueio deve ser no futuro.")
        LocalDate dataInicio,

        @NotNull(message = "A data final do bloqueio é obrigatória.")
        @Future(message = "A data final do bloqueio deve ser no futuro.")
        LocalDate dataFim,


        @NotBlank(message = "O motivo é obrigatório")
        String motivo
) {
        // Metodo auxiliar que verifica se dataInicio é antes que dataFim
        // @AssertTrue: valida se o método retorna 'true'. Usado para regras que
        // envolvem mais de um campo (ex: dataFim >= diaInicio), o que @NotNull/
        // @FutureOrPresent sozinhos não conseguem checar. O método precisa
        // começar com "is" e retornar boolean.
        @AssertTrue(message = "Data de início deve ser antes da data de fim")
        public boolean isPeriodoValido(){
                return dataInicio.isBefore(dataFim);
        }
}
