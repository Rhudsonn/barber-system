package com.zheiry.barbersystem.security;

import java.time.Duration;

public class LimiteTentativasExcedidoException extends RuntimeException {
    private final Duration aguardar;

    public LimiteTentativasExcedidoException(Duration aguardar) {
        super("Limite temporário de tentativas excedido.");
        this.aguardar = aguardar;
    }

    public long retryAfterSegundos() {
        return Math.max(1, (aguardar.toSeconds() + (aguardar.toMillisPart() > 0 ? 1 : 0)));
    }
}
