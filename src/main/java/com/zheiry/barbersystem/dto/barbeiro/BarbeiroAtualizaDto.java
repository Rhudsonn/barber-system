package com.zheiry.barbersystem.dto.barbeiro;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record BarbeiroAtualizaDto(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotNull(message = "A data de nascimento é obrigatória")
        @Past(message = "A data de nascimento deve ser uma data no passado")
        LocalDate dataNascimento,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$",
                message = "Telefone inválido. Formato esperado: (XX) 9XXXX-XXXX"
        )
        String telefone,

        @NotBlank(message = "A especialidade é obrigatória")
        String especialidade,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email
) {
}
