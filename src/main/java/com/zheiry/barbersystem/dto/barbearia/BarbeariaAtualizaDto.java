package com.zheiry.barbersystem.dto.barbearia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BarbeariaAtualizaDto(

        @NotBlank(message = "Nome fantasia é obrigatório.")
        @Size(max = 120, message = "Nome fantasia deve ter no máximo 120 caracteres.")
        String nomeFantasia,

        @NotBlank(message = "Razão social é obrigatória.")
        @Size(max = 150, message = "Razão social deve ter no máximo 150 caracteres.")
        String razaoSocial,

        @NotBlank(message = "Telefone é obrigatório.")
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
        String telefone,

        @Size(max = 20, message = "WhatsApp deve ter no máximo 20 caracteres.")
        String whatsapp,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 120, message = "E-mail deve ter no máximo 120 caracteres.")
        String email,

        @NotBlank(message = "Endereço é obrigatório.")
        @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres.")
        String endereco,

        @NotBlank(message = "Cidade é obrigatória.")
        @Size(max = 80, message = "Cidade deve ter no máximo 80 caracteres.")
        String cidade,

        @NotBlank(message = "Estado (UF) é obrigatório.")
        @Size(min = 2, max = 2, message = "Estado deve ser a UF com 2 caracteres.")
        String estado,

        @NotBlank(message = "CEP é obrigatório.")
        @Size(min = 8, max = 9, message = "CEP inválido.")
        String cep,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
        String descricao

) {
}
