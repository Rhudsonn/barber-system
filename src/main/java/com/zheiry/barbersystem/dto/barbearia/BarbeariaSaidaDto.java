package com.zheiry.barbersystem.dto.barbearia;

import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoSaidaDto;

import java.util.List;

public record BarbeariaSaidaDto(


        Long id,
        String nomeFantasia,
        String razaoSocial,
        String cnpj,
        String telefone,
        String whatsapp,
        String email,
        String endereco,
        String cidade,
        String estado,
        String cep,
        String descricao,
        List<HorarioFuncionamentoSaidaDto> horarios

) {
}
