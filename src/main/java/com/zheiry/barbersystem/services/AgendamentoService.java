package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.agendamento.AgendamentoDto;
import com.zheiry.barbersystem.dto.agendamento.AgendamentoSaidaDto;
import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.entities.ServicoEntity;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.AgendamentoMapper;
import com.zheiry.barbersystem.repositories.AgendamentoRepository;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.repositories.ClienteRepository;
import com.zheiry.barbersystem.repositories.ServicoRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final ServicoRepository servicoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository, BarbeiroRepository barbeiroRepository, ServicoRepository servicoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.servicoRepository = servicoRepository;
    }

    // Verifica se existe conflito de horário para o barbeiro.
    //Busca os agendamentos do dia e verifica a sobreposição em memória.
    public boolean existeConflito(Long barbeiroId, LocalDateTime inicio, LocalDateTime fim, Long agendamentoIdExcluido) {

        LocalDate data = inicio.toLocalDate();
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        List<AgendamentoEntity> agendamentos = agendamentoRepository.findByBarbeiroIdAndDataHoraInicioBetween(barbeiroId, inicioDoDia, fimDoDia);

        return agendamentos.stream()
                .filter(a -> a.getStatusAgendamento() != StatusAgendamento.CANCELADO_CLIENTE && a.getStatusAgendamento() != StatusAgendamento.CANCELADO_BARBEIRO)
                .filter(a -> agendamentoIdExcluido == null || !a.getId().equals(agendamentoIdExcluido))
                .anyMatch(a -> a.getDataHoraInicio().isBefore(fim) && a.getDataHoraFim().isBefore(inicio));
    }


    // Criar agendamento
    // Valida cliente, barbeiro e serviço, calcula o horário de fim, e verifica se há conflito de horário.
    @Transactional
    public AgendamentoSaidaDto criar(AgendamentoDto dto) throws BadRequestException {

        ClienteEntity cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        if (cliente.getStatus() != Status.ATIVO) {
            throw new BadRequestException("O cliente está inativo e não pode realizar agendamentos.");
        }

        BarbeiroEntity barbeiro = barbeiroRepository.findById(dto.barbeiroId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro não encontrado."));

        if (barbeiro.getStatus() != Status.ATIVO) {
            throw new BadRequestException("O barbeiro está inativo e não pode receber agendamentos.");
        }

        ServicoEntity servico = servicoRepository.findById(dto.servicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        if (servico.getStatus() != Status.ATIVO) {
            throw new BadRequestException("O serviço está inativo e não pode ser agendado.");
        }

        LocalDateTime dataHoraInicio = dto.dataHoraInicio();
        LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(servico.getDuracaoEmMinutos());

        // Verifica conflito de horário
        if (existeConflito(barbeiro.getId(), dataHoraInicio, dataHoraFim, null)) {
            throw new BadRequestException("Já existe um agendamento para este barbeiro neste horário.");
        }

        AgendamentoEntity entity = AgendamentoMapper.toEntity(dto, cliente, barbeiro, servico);
        AgendamentoEntity salvo = agendamentoRepository.save(entity);

        return AgendamentoMapper.toSaidaDto(salvo);
    }


}
