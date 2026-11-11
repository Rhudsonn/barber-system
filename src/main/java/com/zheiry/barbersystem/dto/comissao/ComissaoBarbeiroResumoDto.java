package com.zheiry.barbersystem.dto.comissao;

import java.math.BigDecimal;

public record ComissaoBarbeiroResumoDto(
        Long barbeiroId,
        String barbeiroNome,
        long quantidadeServicosConcluidos,
        BigDecimal valorTotalServicos,
        BigDecimal valorComissaoGerada
) {
}
