package com.zheiry.barbersystem.dto.horarioFuncionamento;

import com.zheiry.barbersystem.enums.DiaSemana;

import java.time.LocalTime;

public record HorarioFuncionamentoSaidaDto(


        Long id,
        Long barbeariaId,
        String barbeariaNomeFantasia,
        DiaSemana diaSemana,
        LocalTime horarioInicio,
        LocalTime horarioFim

) {
}
