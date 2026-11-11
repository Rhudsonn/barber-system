package com.zheiry.barbersystem.dto.comissao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComissaoDetalheBarbeiroDto(
        Long comissaoId,
        Long agendamentoId,
        Long servicoId,
        String servicoNome,
        LocalDateTime concluidoEm,
        BigDecimal valorServico,
        BigDecimal porcentagemBarbeiro,
        BigDecimal valorComissao
) {
}
