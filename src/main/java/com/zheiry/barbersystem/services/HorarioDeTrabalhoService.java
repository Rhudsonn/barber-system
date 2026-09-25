package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoAtualizarDto;
import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoDto;
import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoSaidaDto;
import com.zheiry.barbersystem.entities.AgendamentoEntity;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.entities.HorarioDeTrabalhoEntity;
import com.zheiry.barbersystem.enums.DiaSemana;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.enums.StatusAgendamento;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.BusinessException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.HorarioDeTrabalhoMapper;
import com.zheiry.barbersystem.repositories.AgendamentoRepository;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import com.zheiry.barbersystem.repositories.HorarioDeTrabalhoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import java.time.DayOfWeek;

@Service
public class HorarioDeTrabalhoService {

    private final HorarioDeTrabalhoRepository horarioDeTrabalhoRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final AgendamentoRepository agendamentoRepository;

    public HorarioDeTrabalhoService(HorarioDeTrabalhoRepository horarioDeTrabalhoRepository, BarbeiroRepository barbeiroRepository,
                                    AgendamentoRepository agendamentoRepository) {
        this.horarioDeTrabalhoRepository = horarioDeTrabalhoRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.agendamentoRepository = agendamentoRepository;
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
        boolean existeSobreposicao = horarioDeTrabalhoRepository.existsByBarbeiroIdAndDiaSemanaAndIdNotAndHorarioInicioLessThanAndHorarioFimGreaterThan(
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
   @Transactional
    public void excluirHorarioDeTrabalho(Long id){

        // Buscar horario - Verificar se existe.
       HorarioDeTrabalhoEntity horario = horarioDeTrabalhoRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Horário de trabalho não encontrado."));

       Long barbeiroId = horario.getBarbeiro().getId();
       DiaSemana diaDoHorario = horario.getDiaSemana();
       LocalTime inicio = horario.getHorarioInicio();
       LocalTime fim = horario.getHorarioFim();

       // Status que nao devem bloquear o excluir.
       List<StatusAgendamento> statusIgnorados = List.of(StatusAgendamento.CANCELADO_CLIENTE, StatusAgendamento.CANCELADO_BARBEIRO);

       // Busca agendamentos futuros do barbeiro.
       List<AgendamentoEntity> agendamentosFuturos = agendamentoRepository.findByBarbeiroIdAndDataHoraInicioAfterAndStatusAgendamentoNotIn(
               barbeiroId,
               LocalDateTime.now(),
               statusIgnorados
       );

       // Verifica se algum agendamento futuro conflita com este horario.
       boolean temComflito = agendamentosFuturos.stream().anyMatch(agendamentoEntity -> {

           DiaSemana diaDoAgendamento = converterParaDiaSemana(agendamentoEntity.getDataHoraInicio().getDayOfWeek());

           LocalTime horaDoAgendamento = agendamentoEntity.getDataHoraInicio().toLocalTime();
           boolean mesmoDia = diaDoAgendamento == diaDoHorario;

           boolean dentroDoHorario =
                   !horaDoAgendamento.isBefore(inicio) &&
                           horaDoAgendamento.isBefore(fim);

           return mesmoDia && dentroDoHorario;
       });
       if (temComflito) {
           throw new BusinessException("Não é possível excluir. Existem agendamentos futuros neste horário de trabalho");
       }

       horarioDeTrabalhoRepository.delete(horario);
   }

   // metodo auxiliar de conversao - ele me ajuda convertendo o dia da semana para que eu utilize no metodo acima de excluir horario.
   private DiaSemana converterParaDiaSemana(DayOfWeek dayOfWeek) {
       return switch (dayOfWeek) {
           case MONDAY    -> DiaSemana.SEGUNDA;
           case TUESDAY   -> DiaSemana.TERCA;
           case WEDNESDAY -> DiaSemana.QUARTA;
           case THURSDAY  -> DiaSemana.QUINTA;
           case FRIDAY    -> DiaSemana.SEXTA;
           case SATURDAY  -> DiaSemana.SABADO;
           case SUNDAY    -> DiaSemana.DOMINGO;
       };
   }

    // Listar horarios Por Barbeiro (barbeiroId)


    // REGRAS DE NEGOCIO
    // Esta dentro do horario de trabalho (barbeiroId, dataHora - boolean)
    // Obter horario do dia (barbeiroId, diaSemana) - horario de trabalho null
    // Gerar Slots disponiveis (barbeiroId, data, duracaoServico) - List<LocalTime>
    // Validar Sobre posicao (barbeiroId, diaSemana, inicio, fim) - boolean
}
