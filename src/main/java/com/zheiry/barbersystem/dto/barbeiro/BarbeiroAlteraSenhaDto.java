package com.zheiry.barbersystem.dto.barbeiro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BarbeiroAlteraSenhaDto(

        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{8,}$",
                message = "A nova senha deve conter no mínimo 8 caracteres, 1 letra maiúscula, 1 letra minúscula, 1 número e 1 caractere especial"
        )
        String novaSenha,

        @NotBlank(message = "A confirmação da nova senha é obrigatória")
        String confirmaNovaSenha
) {
}
