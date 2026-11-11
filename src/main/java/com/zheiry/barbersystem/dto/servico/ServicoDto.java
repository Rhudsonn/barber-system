package com.zheiry.barbersystem.dto.servico;

import com.zheiry.barbersystem.enums.Categoria;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

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
        Integer duracaoEmMinutos,

        @DecimalMin(value = "0.00", message = "A porcentagem da barbearia não pode ser negativa.")
        @DecimalMax(value = "100.00", message = "A porcentagem da barbearia não pode ultrapassar 100%.")
        @Digits(integer = 3, fraction = 2, message = "A porcentagem deve ter no máximo duas casas decimais.")
        BigDecimal porcentagemBarbearia

) {
}
