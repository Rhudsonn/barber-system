package com.zheiry.barbersystem.dto.agendamento;

import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.enums.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AgendamentoSaidaDto(

        Long id,

        // Dados resumidos do Cliente
        Long clienteId,
        String clienteNome,
        String clienteTelefone,

        // Dados resumidos do Barbeiro
        Long barbeiroId,
        String barbeiroNome,

        // Dados resumidos do Serviço
        Long servicoId,
        String servicoNome,
        Integer servicoDuracaoMinutos,
        Double servicoPreco,

        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,

        StatusAgendamento statusAgendamento,

        FormaPagamento formaPagamento,
        BigDecimal valorTotalComanda,

        String observacao,

        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm

) {
}
