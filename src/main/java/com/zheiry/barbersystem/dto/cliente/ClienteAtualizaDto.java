package com.zheiry.barbersystem.dto.cliente;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ClienteAtualizaDto(


        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,


        @Past(message = "A data de nascimento deve ser uma data no passado")
        LocalDate dataNascimento,


        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$",
                message = "Telefone inválido. Formato esperado: (XX) 9XXXX-XXXX"
        )
        String telefone,


        @Email(message = "Formato de e-mail inválido")
        String email
) {
}
