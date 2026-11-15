package com.zheiry.barbersystem.util;

import com.zheiry.barbersystem.exception.BadRequestException;

/** Normaliza números brasileiros para o formato internacional exigido pelo wa.me. */
public final class TelefoneWhatsApp {
    private TelefoneWhatsApp() { }

    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) return null;
        if (valor.length() > 25 || !valor.matches("[+0-9() .-]+")) {
            throw new BadRequestException("Informe um número de WhatsApp brasileiro válido.");
        }

        String digitos = valor.replaceAll("\\D", "");
        String nacional;
        if (digitos.startsWith("55") && (digitos.length() == 12 || digitos.length() == 13)) {
            nacional = digitos.substring(2);
        } else if (digitos.length() == 10 || digitos.length() == 11) {
            nacional = digitos;
        } else {
            throw new BadRequestException("Informe um número de WhatsApp brasileiro válido.");
        }

        if (!nacional.matches("[1-9][1-9](?:[2-5][0-9]{7}|9[0-9]{8})")) {
            throw new BadRequestException("Informe um número de WhatsApp brasileiro válido.");
        }
        return "55" + nacional;
    }
}
