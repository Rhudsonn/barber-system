package com.zheiry.barbersystem.dto.notificacao;

import com.zheiry.barbersystem.enums.CanalNotificacao;

public record PreferenciaNotificacaoSaidaDto(
        CanalNotificacao canal,
        boolean autorizada
) {
}
