package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.notificacao.ResultadoEnvio;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.CanalNotificacao;

public interface CanalNotificador {
    CanalNotificacao canal();
    ResultadoEnvio enviar(NotificacaoEntity notificacao);
}
