package com.zheiry.barbersystem.dto.barbearia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BarbeariaAtualizaDto(

        @Pattern(regexp = "(?s).*\\S.*", message = "Nome fantasia não pode ficar em branco.")
        @Size(max = 120, message = "Nome fantasia deve ter no máximo 120 caracteres.")
        String nomeFantasia,

        @Pattern(regexp = "(?s).*\\S.*", message = "Razão social não pode ficar em branco.")
        @Size(max = 150, message = "Razão social deve ter no máximo 150 caracteres.")
        String razaoSocial,

        @Pattern(regexp = "(?s).*\\S.*", message = "Telefone não pode ficar em branco.")
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
        String telefone,

        @Size(max = 2048, message = "O link do Instagram deve ter no máximo 2048 caracteres.")
        String instagram,

        @Size(max = 25, message = "O WhatsApp deve ter no máximo 25 caracteres.")
        @Pattern(regexp = "[+0-9() .-]*", message = "Informe um número de WhatsApp válido.")
        String whatsapp,

        @Pattern(regexp = "(?s).*\\S.*", message = "E-mail não pode ficar em branco.")
        @Email(message = "E-mail inválido.")
        @Size(max = 120, message = "E-mail deve ter no máximo 120 caracteres.")
        String email,

        @Pattern(regexp = "(?s).*\\S.*", message = "Endereço não pode ficar em branco.")
        @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres.")
        String endereco,

        @Pattern(regexp = "(?s).*\\S.*", message = "Cidade não pode ficar em branco.")
        @Size(max = 80, message = "Cidade deve ter no máximo 80 caracteres.")
        String cidade,

        @Pattern(regexp = "(?s).*\\S.*", message = "Estado não pode ficar em branco.")
        @Size(min = 2, max = 2, message = "Estado deve ser a UF com 2 caracteres.")
        String estado,

        @Pattern(regexp = "(?s).*\\S.*", message = "CEP não pode ficar em branco.")
        @Size(min = 8, max = 9, message = "CEP inválido.")
        String cep,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
        String descricao,

        @Size(max = 2048, message = "A URL do logo deve ter no máximo 2048 caracteres.")
        String logoUrl

) {
}
