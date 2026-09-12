package com.zheiry.barbersystem.dto.servico;

import com.zheiry.barbersystem.enums.Categoria;


public record ServicoSaidaDto(

        Long id,

        String nome,
        String descrica,
        Categoria categoria,
        Double preco,
        Integer duracaoEmMinutos

) {
}
