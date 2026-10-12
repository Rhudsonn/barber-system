package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoAtualizaDto;
import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoDto;
import com.zheiry.barbersystem.dto.horarioFuncionamento.HorarioFuncionamentoSaidaDto;
import com.zheiry.barbersystem.entities.BarbeariaEntity;
import com.zheiry.barbersystem.entities.HorarioFuncionamentoEntity;
import com.zheiry.barbersystem.enums.DiaSemana;
import com.zheiry.barbersystem.exception.BusinessException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.HorarioFuncionamentoMapper;
import com.zheiry.barbersystem.repositories.BarbeariaRepository;
import com.zheiry.barbersystem.repositories.HorarioFuncionamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class HorarioFuncionamentoService {

    private final HorarioFuncionamentoRepository horarioFuncionamentoRepository;
    private final BarbeariaRepository barbeariaRepository;

    public HorarioFuncionamentoService(HorarioFuncionamentoRepository horarioFuncionamentoRepository,
                                       BarbeariaRepository barbeariaRepository) {
        this.horarioFuncionamentoRepository = horarioFuncionamentoRepository;
        this.barbeariaRepository = barbeariaRepository;
    }

    @Transactional
    public HorarioFuncionamentoSaidaDto criar(HorarioFuncionamentoDto dto) {
        BarbeariaEntity barbearia = barbeariaRepository.findById(dto.barbeariaId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada."));

        if (!dto.horarioInicio().isBefore(dto.horarioFim())) {
            throw new BusinessException("Horário de início deve ser antes do horário de fim.");
        }

        boolean existeSobreposicao = horarioFuncionamentoRepository
                .existsByBarbeariaIdAndDiaSemanaAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                        dto.barbeariaId(),
                        dto.diaSemana(),
                        dto.horarioFim(),
                        dto.horarioInicio()
                );
        if (existeSobreposicao) {
            throw new BusinessException("Já existe um horário de funcionamento que conflita com este intervalo.");
        }

        HorarioFuncionamentoEntity entity = HorarioFuncionamentoMapper.toEntity(dto, barbearia);
        HorarioFuncionamentoEntity salva = horarioFuncionamentoRepository.save(entity);
        return HorarioFuncionamentoMapper.toSaidaDto(salva);
    }

    @Transactional
    public HorarioFuncionamentoSaidaDto atualizar(Long id, HorarioFuncionamentoAtualizaDto dto) {
        HorarioFuncionamentoEntity horario = horarioFuncionamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horário de funcionamento não encontrado."));

        if (!dto.horarioInicio().isBefore(dto.horarioFim())) {
            throw new BusinessException("Horário de início deve ser antes do horário de fim.");
        }

        boolean existeSobreposicao = horarioFuncionamentoRepository
                .existsByBarbeariaIdAndDiaSemanaAndIdNotAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                        horario.getBarbearia().getId(),
                        horario.getDiaSemana(),
                        id,
                        dto.horarioFim(),
                        dto.horarioInicio()
                );
        if (existeSobreposicao) {
            throw new BusinessException("Já existe um horário de funcionamento que conflita com este intervalo.");
        }

        HorarioFuncionamentoMapper.atualizarEntity(dto, horario);
        HorarioFuncionamentoEntity atualizada = horarioFuncionamentoRepository.save(horario);
        return HorarioFuncionamentoMapper.toSaidaDto(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        HorarioFuncionamentoEntity horario = horarioFuncionamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horário de funcionamento não encontrado."));
        horarioFuncionamentoRepository.delete(horario);
    }

    @Transactional(readOnly = true)
    public List<HorarioFuncionamentoSaidaDto> listarPorBarbearia(Long barbeariaId) {
        if (!barbeariaRepository.existsById(barbeariaId)) {
            throw new ResourceNotFoundException("Barbearia não encontrada.");
        }
        return HorarioFuncionamentoMapper.toSaidaDtoList(
                horarioFuncionamentoRepository.findByBarbeariaId(barbeariaId)
        );
    }

    @Transactional(readOnly = true)
    public List<HorarioFuncionamentoSaidaDto> listarPorBarbeariaEDia(Long barbeariaId, DiaSemana diaSemana) {
        if (!barbeariaRepository.existsById(barbeariaId)) {
            throw new ResourceNotFoundException("Barbearia não encontrada.");
        }
        return HorarioFuncionamentoMapper.toSaidaDtoList(
                horarioFuncionamentoRepository.findByBarbeariaIdAndDiaSemana(barbeariaId, diaSemana)
        );
    }

    @Transactional(readOnly = true)
    public boolean estaAberta(Long barbeariaId, DiaSemana diaSemana, LocalTime hora) {
        List<HorarioFuncionamentoEntity> faixas =
                horarioFuncionamentoRepository.findByBarbeariaIdAndDiaSemana(barbeariaId, diaSemana);

        return faixas.stream().anyMatch(faixa ->
                !hora.isBefore(faixa.getHorarioInicio()) && hora.isBefore(faixa.getHorarioFim())
        );
    }
}
