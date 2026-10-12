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

import java.time.LocalDateTime;
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
            throw new BusinessException("Barbeiro está inativo");
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
    @Transactional
    public BloqueioSaidaDto atualizar(Long id, BloqueioAtualizaDto dto){

        //Busca o bloqueio pelo ID
        BloqueioEntity bloqueio = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bloqueio não encontrad"));

        if (!dto.dataInicio().isBefore(dto.dataFim())) {
            throw new BusinessException("Data de início deve ser antes da data de fim");
        }

        //Pega o ID do barbeiro dono do bloqueio (o barbeiro não muda na atualização).
        Long barbeiroId = bloqueio.getBarbeiro().getId();

        // Sobreposição ignorando o próprio bloqueio
        List<BloqueioEntity> conflitos = repository
                .findByBarbeiroIdAndIdNotAndDataInicioLessThanAndDataFimGreaterThan(
                        barbeiroId,
                        id,
                        dto.dataFim(),
                        dto.dataInicio()
                );

        if (!conflitos.isEmpty()) {
            throw new BusinessException("Já existe um bloqueio que conflita com este período");
        }

        validarAgendamentosNoPeriodo(barbeiroId, dto.dataInicio(), dto.dataFim());

        BloqueioMapper.atualizarEntity(dto, bloqueio);
        BloqueioEntity atualizado = repository.save(bloqueio);
        return BloqueioMapper.toSaidaDto(atualizado);
    }


    // Excluir
    @Transactional
    public void excluir(Long id){
        BloqueioEntity bloqueio = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bloqueio não encontrado"));
        repository.delete(bloqueio);
    }

    // buscarPorId
    @Transactional(readOnly = true)
    public BloqueioSaidaDto buscarPorId(Long id){
        BloqueioEntity bloqueio = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bloqueio não encontrado"));
        return BloqueioMapper.toSaidaDto(bloqueio);
    }

    // listarPorBarbeiro
    @Transactional(readOnly = true)
    public List<BloqueioSaidaDto> listarPorBarbeiro(Long barbeiroId){

        if (!barbeiroRepository.existsById(barbeiroId)) {
            throw new ResourceNotFoundException("Barbeiro não encontrado");
        }

        List<BloqueioEntity> bloqueios = repository.findByBarbeiroId(barbeiroId);
        return BloqueioMapper.toSaidaDtoList(bloqueios);
    }


    // listarPorBarbeiroEPeriodo (útil para agenda)
    @Transactional(readOnly = true)
    public List<BloqueioSaidaDto> listarPorBarbeiroEPeriodo(Long barbeiroId, LocalDateTime inicio, LocalDateTime fim){

        if (!barbeiroRepository.existsById(barbeiroId)) {
            throw new ResourceNotFoundException("Barbeiro não encontrado");
        }

        if (inicio == null || fim == null || !inicio.isBefore(fim)) {
            throw new BusinessException("Período inválido: início deve ser antes do fim");
        }

        // Mesma lógica de sobreposição: bloqueio intersecta o intervalo [inicio, fim]
        List<BloqueioEntity> bloqueios = repository
                .findByBarbeiroIdAndDataInicioLessThanAndDataFimGreaterThan(
                        barbeiroId,
                        fim,
                        inicio
                );

        return BloqueioMapper.toSaidaDtoList(bloqueios);
    }


    // existeBloqueioNoPeriodo
    @Transactional(readOnly = true)
    public boolean existeBloqueioNoPeriodo(Long barbeiroId,
                                           LocalDateTime inicio,
                                           LocalDateTime fim) {
        return repository.existsByBarbeiroIdAndDataInicioLessThanAndDataFimGreaterThan(
                barbeiroId,
                fim,
                inicio
        );
    }



    // Método privado de apoio
    private void validarAgendamentosNoPeriodo(Long barbeiroId, LocalDateTime inicio, LocalDateTime fim) {
        List<StatusAgendamento> statusIgnorados = List.of(
                StatusAgendamento.CANCELADO_CLIENTE,
                StatusAgendamento.CANCELADO_BARBEIRO
        );

        List<AgendamentoEntity> agendamentosNoPeriodo = agendamentoRepository
                .findByBarbeiroIdAndDataHoraInicioBetweenAndStatusAgendamentoNotIn(
                        barbeiroId,
                        inicio,
                        fim,
                        statusIgnorados
                );

        if (!agendamentosNoPeriodo.isEmpty()) {
            throw new BusinessException(
                    "Existem agendamentos neste período. Cancele-os antes de atualizar o bloqueio"
            );
        }
    }
}
