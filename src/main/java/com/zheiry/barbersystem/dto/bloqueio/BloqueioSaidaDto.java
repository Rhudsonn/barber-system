package com.zheiry.barbersystem.dto.bloqueio;


import java.time.LocalDateTime;

public record BloqueioSaidaDto(

        Long id,

        Long barbeiroId,
        String nomeBarbeiro,

        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String motivo
) {
}
