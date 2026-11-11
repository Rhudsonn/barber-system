package com.zheiry.barbersystem.exception;

public class FalhaEnvioNotificacaoException extends RuntimeException {
    private final boolean retentavel;

    public FalhaEnvioNotificacaoException(String mensagem, boolean retentavel) {
        super(mensagem);
        this.retentavel = retentavel;
    }

    public boolean isRetentavel() { return retentavel; }
}
