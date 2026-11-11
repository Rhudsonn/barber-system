package com.zheiry.barbersystem.dto.servico;

import com.zheiry.barbersystem.enums.Categoria;
import java.math.BigDecimal;


public record ServicoSaidaDto(

        Long id,

        String nome,
        String descrica,
        Categoria categoria,
        Double preco,
        Integer duracaoEmMinutos,
        BigDecimal porcentagemBarbearia,
        BigDecimal porcentagemProfissional,
        BigDecimal valorBarbearia,
        BigDecimal valorProfissional

) {
}
