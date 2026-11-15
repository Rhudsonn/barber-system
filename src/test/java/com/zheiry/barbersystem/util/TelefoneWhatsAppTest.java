package com.zheiry.barbersystem.util;

import com.zheiry.barbersystem.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelefoneWhatsAppTest {
    @Test
    void normalizaNumeroBrasileiroNacionalParaDdi55() {
        assertEquals("5511999999999", TelefoneWhatsApp.normalizar("(11) 99999-9999"));
    }

    @Test
    void preservaNumeroBrasileiroJaComDdi55() {
        assertEquals("5511999999999", TelefoneWhatsApp.normalizar("+55 (11) 99999-9999"));
    }

    @Test
    void aceitaCampoOpcionalVazio() {
        assertNull(TelefoneWhatsApp.normalizar("  "));
    }

    @Test
    void rejeitaTextoEUrlsAntesDeGerarContato() {
        assertThrows(BadRequestException.class, () -> TelefoneWhatsApp.normalizar("https://evil.example/wa.me/5511999999999"));
        assertThrows(BadRequestException.class, () -> TelefoneWhatsApp.normalizar("5500999999999"));
    }
}
