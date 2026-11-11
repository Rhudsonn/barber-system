package com.zheiry.barbersystem.dto.comissao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComissaoDetalheAdministradorDto(
        Long comissaoId,
        Long agendamentoId,
        Long barbeiroId,
        String barbeiroNome,
        Long servicoId,
        String servicoNome,
        LocalDateTime concluidoEm,
        BigDecimal valorServico,
        BigDecimal porcentagemBarbearia,
        BigDecimal porcentagemBarbeiro,
        BigDecimal valorBarbearia,
        BigDecimal valorBarbeiro
) {
}
