package com.zheiry.barbersystem.enums;

public enum StatusAgendamento {

    AGENDADO,      // confirmado
    CONFIRMADO,    // cliente confirmou (opcional)
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO_CLIENTE,
    CANCELADO_BARBEIRO,
    NAO_COMPARECEU  // no-show

}
