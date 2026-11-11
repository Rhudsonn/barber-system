package com.zheiry.barbersystem.dto.notificacao;

import com.zheiry.barbersystem.enums.CanalNotificacao;
import jakarta.validation.constraints.NotNull;

public record PreferenciaNotificacaoAtualizaDto(
        @NotNull(message = "O canal de notificação é obrigatório.")
        CanalNotificacao canal,
        boolean autorizada
) {
}
