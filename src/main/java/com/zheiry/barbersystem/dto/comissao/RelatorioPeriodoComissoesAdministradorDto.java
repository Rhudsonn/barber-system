package com.zheiry.barbersystem.dto.comissao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioPeriodoComissoesAdministradorDto(
        LocalDate inicio,
        LocalDate fim,
        long quantidadeServicosConcluidos,
        BigDecimal valorTotalServicos,
        BigDecimal valorTotalBarbearia,
        BigDecimal valorTotalComissoesBarbeiros,
        List<ComissaoBarbeiroResumoDto> barbeiros,
        List<ComissaoDetalheAdministradorDto> detalhes
) {}
