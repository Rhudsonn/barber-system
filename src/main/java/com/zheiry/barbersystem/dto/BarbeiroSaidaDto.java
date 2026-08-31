package com.zheiry.barbersystem.dto;

import java.time.LocalDate;

public record BarbeiroSaidaDto(

        // Porque usar ID no dto de saida:
        //Nas tabelas e listas renderizadas no Thymeleaf (th:each),
        // você precisa construir links dinâmicos para agir sobre um registro específico.
        // O Thymeleaf utiliza o id presente no DTO para preencher os parâmetros da URL:
        Long id,

        String nome,
        LocalDate dataNascimento,
        String telefone,
        String especialidade,
        String email
) {
}
