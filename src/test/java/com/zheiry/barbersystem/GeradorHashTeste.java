
package com.zheiry.barbersystem;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorHashTeste {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        java.io.Console console = System.console();

        if (console == null) {
            throw new IllegalStateException(
                    "Execute pelo terminal para informar a senha."
            );
        }

        char[] senha = console.readPassword("Senha do administrador: ");

        try {
            String hash = encoder.encode(new String(senha));
            System.out.println("Hash BCrypt: " + hash);
        } finally {
            java.util.Arrays.fill(senha, '\0');
        }
    }
}
