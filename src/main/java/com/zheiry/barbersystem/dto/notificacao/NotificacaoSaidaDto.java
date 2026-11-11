package com.zheiry.barbersystem.dto.notificacao;

import com.zheiry.barbersystem.enums.CanalNotificacao;
import com.zheiry.barbersystem.enums.StatusNotificacao;
import com.zheiry.barbersystem.enums.TipoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoSaidaDto(
        Long id,
        Long agendamentoId,
        TipoNotificacao tipo,
        CanalNotificacao canal,
        StatusNotificacao status,
        LocalDateTime programadaPara,
        LocalDateTime tentadaEm,
        LocalDateTime enviadaEm,
        int quantidadeTentativas,
        String erro
) {
}
