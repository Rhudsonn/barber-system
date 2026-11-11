package com.zheiry.barbersystem.dto.agendamento;

import com.zheiry.barbersystem.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;

public record FecharComandaDto(
        @NotNull(message = "A forma de pagamento é obrigatória.")
        FormaPagamento formaPagamento,
        Long servicoAdicionalId
) {
}
