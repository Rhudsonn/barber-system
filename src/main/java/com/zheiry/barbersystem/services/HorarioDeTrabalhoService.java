package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoAtualizarDto;
import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoDto;
import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoSaidaDto;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.HorarioDeTrabalhoEntity;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.BusinessException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.HorarioDeTrabalhoMapper;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.repositories.HorarioDeTrabalhoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HorarioDeTrabalhoService {

    private final HorarioDeTrabalhoRepository horarioDeTrabalhoRepository;
    private final BarbeiroRepository barbeiroRepository;

    public HorarioDeTrabalhoService(HorarioDeTrabalhoRepository horarioDeTrabalhoRepository, BarbeiroRepository barbeiroRepository) {
        this.horarioDeTrabalhoRepository = horarioDeTrabalhoRepository;
        this.barbeiroRepository = barbeiroRepository;
    }

    // Criar horario de trabalho (barbeiroId, diaSemana, inicio, fim)
    @Transactional
    public HorarioDeTrabalhoSaidaDto criar(HorarioDeTrabalhoDto dto) {

        BarbeiroEntity barbeiro = barbeiroRepository.findById(dto.barbeiroId()).orElseThrow(() -> new ResourceNotFoundException("barbeiro não encontrado."));

        // Verifica se o barbeiro está ativo
        if (barbeiro.getStatus() != Status.ATIVO) {
            throw new BadRequestException("O barbeiro está inativo e não pode realizar agendamentos.");
        }

        // Valida se horárioInicio < horarioFim (já tem no DTO, mas reforça)
        if (!dto.horarioInicio().isBefore(dto.horarioFim())){
            throw new BusinessException("Horário de início deve ser antes do horário de fim");
        }

        // Verifica sobreposição de horários no mesmo dia
        boolean existeSobreposicao = horarioDeTrabalhoRepository
                .existsByBarbeiroIdAndDiaSemanaAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                dto.barbeiroId(),
                dto.diaSemana(),
                dto.horarioFim(),      // novoFim > inicioExistente
                dto.horarioInicio()    // novoInicio < fimExistente
        );
        if (existeSobreposicao){
            throw new BusinessException("Já existe um horário cadastrado que conflita com este intervalo");
        }

        //Converte DTO → Entity
        HorarioDeTrabalhoEntity horario = HorarioDeTrabalhoMapper.toEntity(dto, barbeiro);
        HorarioDeTrabalhoEntity salva = horarioDeTrabalhoRepository.save(horario);
        return HorarioDeTrabalhoMapper.toSaidaDto(salva);
    }


    // Atualizar Horario (id, inicio, fim)
    @Transactional
    public HorarioDeTrabalhoSaidaDto atualizarHorarioDeTrabalho(Long id, HorarioDeTrabalhoAtualizarDto dto){

        // Verifica se Horario (Id) existe.
        HorarioDeTrabalhoEntity horario = horarioDeTrabalhoRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Horário não encontrado."));

        // Valida se horarioInicio < horarioFim.
        if (!dto.horarioInicio().isBefore(dto.horarioFim())){
            throw new BusinessException("Horário de início deve ser antes do horário de fim");
        }


        // Verifica se existe sobreposição com OUTROS horários do mesmo barbeiro, no mesmo dia, ignorando o próprio registro
        boolean existeSobreposicao = horarioDeTrabalhoRepository.existsByBarbeiroIdAndDiaSemanaAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                        horario.getBarbeiro().getId(),
                        horario.getDiaSemana(),
                        id,                      // ignora o próprio horário
                        dto.horarioFim(),        // novo fim
                        dto.horarioInicio()
                );
        if (existeSobreposicao) {
            throw new BusinessException("Já existe um horário cadastrado que conflita com este intervalo");
        }

        // Aplica apenas os campos que vieram preenchidos do DTO.
        HorarioDeTrabalhoMapper.atualizarEntity(dto, horario);
        horarioDeTrabalhoRepository.save(horario);
        return HorarioDeTrabalhoMapper.toSaidaDto(horario);
    }



    // Excluir horario (id)
    // Listar horarios Por Barbeiro (barbeiroId)


    // REGRAS DE NEGOCIO
    // Esta dentro do horario de trabalho (barbeiroId, dataHora - boolean)
    // Obter horario do dia (barbeiroId, diaSemana) - horario de trabalho null
    // Gerar Slots disponiveis (barbeiroId, data, duracaoServico) - List<LocalTime>
    // Validar Sobre posicao (barbeiroId, diaSemana, inicio, fim) - boolean
}
