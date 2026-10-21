package com.zheiry.barbersystem.dto.cliente;

import com.zheiry.barbersystem.enums.Status;

public record ClienteSaidaResumoDto(

        Long id,
        String nome,
        String telefone,
        Status status

) {
}
