package com.zheiry.barbersystem.dto.cliente;

import com.zheiry.barbersystem.enums.Status;

import java.time.LocalDate;

public record ClienteSaidaDto(

        // Porque usar ID no dto de saida:
        //Nas tabelas e listas renderizadas no Thymeleaf (th:each),
        // você precisa construir links dinâmicos para agir sobre um registro específico.
        // O Thymeleaf utiliza o id presente no DTO para preencher os parâmetros da URL:
        Long id,

        String nome,
        LocalDate dataNascimento,
        String telefone,
        String email,

        Status status
) {
}
