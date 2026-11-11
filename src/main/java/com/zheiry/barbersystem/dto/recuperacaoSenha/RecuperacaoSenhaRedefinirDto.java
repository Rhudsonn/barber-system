package com.zheiry.barbersystem.dto.recuperacaoSenha;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RecuperacaoSenhaRedefinirDto(
        @NotBlank(message = "O token de recuperação é obrigatório.")
        String token,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{8,}$",
                message = "A senha deve conter no mínimo 8 caracteres, 1 letra maiúscula, 1 letra minúscula, 1 número e 1 caractere especial.")
        String novaSenha,

        @NotBlank(message = "A confirmação da nova senha é obrigatória.")
        String confirmaNovaSenha
) {
}
