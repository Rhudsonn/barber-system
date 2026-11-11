package com.zheiry.barbersystem.dto.agendamento;

import com.zheiry.barbersystem.enums.FormaPagamento;
import com.zheiry.barbersystem.enums.TipoHistoricoAgendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoAgendamentoDto(
        Long id,
        TipoHistoricoAgendamento tipoEvento,
        LocalDateTime registradoEm,
        Long usuarioId,
        String usuarioEmail,
        String perfilUsuario,
        Long clienteId,
        String clienteNome,
        Long barbeiroId,
        String barbeiroNome,
        Long servicoId,
        String servicoNome,
        BigDecimal valorServico,
        Long servicoAdicionalId,
        String servicoAdicionalNome,
        BigDecimal valorServicoAdicional,
        LocalDateTime dataHoraAnterior,
        LocalDateTime dataHoraNova,
        FormaPagamento formaPagamento,
        BigDecimal valorTotalComanda
) {
}
