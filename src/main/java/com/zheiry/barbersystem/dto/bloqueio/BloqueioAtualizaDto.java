package com.zheiry.barbersystem.dto.bloqueio;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDateTime;

public record BloqueioAtualizaDto(

        @NotNull(message = "Data de início é obrigatória.")
        @Future(message = "A data do bloqueio deve ser no futuro.")
        LocalDateTime dataInicio,

        @NotNull(message = "Data de fim é obrigatória.")
        @Future(message = "A data final do bloqueio deve ser no futuro.")
        LocalDateTime dataFim,

        @NotBlank(message = "Motivo é obrigatório.")
        String motivo
) {
        // Validação customizada que garante consistência entre as datas de início, e fim de um período
        @AssertTrue(message = "Data de início deve ser antes ou igual à data de fim.")
        public boolean isPeriodoValido() {
                // Se dataInicio ou dataFim forem nulas, o metodo retorna, "false"
                return dataInicio != null && dataFim != null
                        && !dataInicio.isAfter(dataFim);  // Verifica se a data de início NÃO é posterior à data de fim
        }
}
