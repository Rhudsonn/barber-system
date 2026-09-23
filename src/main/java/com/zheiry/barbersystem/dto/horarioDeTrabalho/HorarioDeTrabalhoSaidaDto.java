package com.zheiry.barbersystem.dto.horarioDeTrabalho;

import com.zheiry.barbersystem.enums.DiaSemana;

import java.time.LocalTime;

public record HorarioDeTrabalhoSaidaDto(

        Long id,

        // Dados resumidos do Barbeiro
        Long barbeiroId,
        String barbeiroNome,

        DiaSemana diaSemana,
        LocalTime horarioInicio,
        LocalTime horarioFim




) {
}
