package com.zheiry.barbersystem.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginEntradaDto(

        @NotBlank(message = "E-mail é obrigatório")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String senha
) {
}
