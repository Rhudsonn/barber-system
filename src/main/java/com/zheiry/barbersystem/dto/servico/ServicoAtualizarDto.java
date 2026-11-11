package com.zheiry.barbersystem.dto.servico;

import com.zheiry.barbersystem.enums.Categoria;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ServicoAtualizarDto(


        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
        String nome,

        @JsonAlias("descricao")
        String descrica,

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
