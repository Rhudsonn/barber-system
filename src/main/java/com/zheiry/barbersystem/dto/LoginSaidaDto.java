package com.zheiry.barbersystem.dto;

public record LoginSaidaDto(

        String token,
        String tipo, // "Bearer"
        Long usuarioId,
        String perfil
) {
}
