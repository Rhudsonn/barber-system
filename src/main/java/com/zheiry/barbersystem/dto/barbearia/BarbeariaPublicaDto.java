package com.zheiry.barbersystem.dto.barbearia;

/** Campos necessários à identificação pública da barbearia e à reserva de horários. */
public record BarbeariaPublicaDto(
        Long id,
        String nomeFantasia,
        String logoUrl,
        String descricao,
        String endereco,
        String cidade,
        String whatsapp
) {
}
