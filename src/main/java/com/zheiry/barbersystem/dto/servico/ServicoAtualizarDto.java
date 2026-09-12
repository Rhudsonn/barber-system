package com.zheiry.barbersystem.dto.servico;

import com.zheiry.barbersystem.enums.Categoria;
import jakarta.validation.constraints.*;

public record ServicoAtualizarDto(


        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
        String nome,

        String descrica,

        Categoria categoria,

        @Positive(message = "O preço deve ser maior que zero.")
        Double preco,

        @Min(value = 1, message = "A duração deve ser de pelo menos 1 minuto.")
        Integer duracaoEmMinutos

) {
}
