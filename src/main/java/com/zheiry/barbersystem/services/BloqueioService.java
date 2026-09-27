package com.zheiry.barbersystem.services;


import com.zheiry.barbersystem.dto.bloqueio.BloqueioAtualizaDto;
import com.zheiry.barbersystem.dto.bloqueio.BloqueioDto;
import com.zheiry.barbersystem.dto.bloqueio.BloqueioSaidaDto;
import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.BloqueioEntity;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.exception.BusinessException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.BloqueioMapper;
import com.zheiry.barbersystem.repositories.AgendamentoRepository;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.repositories.BloqueioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BloqueioService {

    private final BloqueioRepository repository;
    private final BarbeiroRepository barbeiroRepository;
    private final AgendamentoRepository agendamentoRepository;

    public BloqueioService(BloqueioRepository repository, BarbeiroRepository barbeiroRepository, AgendamentoRepository agendamentoRepository) {
        this.repository = repository;
        this.barbeiroRepository = barbeiroRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // Criar
    @Transactional
    public BloqueioSaidaDto criar(BloqueioDto dto){

        // Verifica se Barbeiro existe
        BarbeiroEntity barbeiro = barbeiroRepository.findById(dto.barbeiroId()).orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado"));

        // Verifica se Barbeiro esta ativo
        if (barbeiro.getStatus() != Status.ATIVO){
            throw new BusinessException("\"Barbeiro não está inativo.");
        }

        // Valida dataInicio < dataFim
        if (!dto.dataInicio().isBefore(dto.dataFim())) {
            throw new BusinessException("Data de início deve ser antes da data de fim");
        }

        // Verifica sobreposição com outros bloqueios do mesmo barbeiro
        List<BloqueioEntity> conflitos = repository
                .findByBarbeiroIdAndDataInicioLessThanAndDataFimGreaterThan(
                        dto.barbeiroId(),
                        dto.dataFim(),      // novo fim
                        dto.dataInicio()    // novo início
                );

        if (!conflitos.isEmpty()) {
            throw new BusinessException("Já existe um bloqueio que conflita com este período");
        }


        // Verifica se existem agendamentos no período
        List<StatusAgendamento> statusIgnorados = List.of(
                StatusAgendamento.CANCELADO_CLIENTE,
                StatusAgendamento.CANCELADO_BARBEIRO
        );

        List<AgendamentoEntity> agendamentosNoPeriodo = agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioBetweenAndStatusAgendamentoNotIn(
                        dto.barbeiroId(),
                        dto.dataInicio(),
                        dto.dataFim(),
                        statusIgnorados
                );

        if (!agendamentosNoPeriodo.isEmpty()) {
            throw new BusinessException(
                    "Existem agendamentos neste período. Cancele-os antes de criar o bloqueio"
            );
        }

        BloqueioEntity entity = BloqueioMapper.toEntity(dto,barbeiro);
        BloqueioEntity salvo = repository.save(entity);
        return BloqueioMapper.toSaidaDto(salvo);
    }


    // Atualizar


    // Excluir
    // buscarPorId
    // listarPorBarbeiro
    // listarPorBarbeiroEPeriodo
    // existeBloqueioNoPeriodo

}
