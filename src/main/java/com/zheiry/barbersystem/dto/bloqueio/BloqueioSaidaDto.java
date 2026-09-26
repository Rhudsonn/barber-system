package com.zheiry.barbersystem.dto.bloqueio;

import java.time.LocalDate;

public record BloqueioSaidaDto(

        Long id,

        Long barbeiroId,
        String nomeBarbeiro,

        LocalDate dataInicio,
        LocalDate dataFim,
        String motivo
) {
}
