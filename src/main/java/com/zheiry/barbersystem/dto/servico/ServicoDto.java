package com.zheiry.barbersystem.dto.servico;

import com.zheiry.barbersystem.enums.Categoria;
import jakarta.validation.constraints.*;

public record ServicoDto(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
        String nome,

        @NotBlank(message = "A descrição do serviço é obrigatória.")
        String descricao,

        @NotNull(message = "A categoria do serviço não pode ser nulo.")
        Categoria categoria,

        @Positive(message = "O preço deve ser maior que zero.")
        Double preco,

        @Min(value = 1, message = "A duração deve ser de pelo menos 1 minuto.")
        Integer duracaoEmMinutos

) {
}
