package com.zheiry.barbersystem.dto.comissao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioPeriodoComissoesBarbeiroDto(
        LocalDate inicio,
        LocalDate fim,
        long quantidadeServicosConcluidos,
        BigDecimal valorTotalServicos,
        BigDecimal valorComissaoGerada,
        List<ComissaoDetalheBarbeiroDto> detalhes
) {}
