package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoAtualizarDto;
import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoDto;
import com.zheiry.barbersystem.dto.horarioDeTrabalho.HorarioDeTrabalhoSaidaDto;
import com.zheiry.barbersystem.services.HorarioDeTrabalhoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/horarios-trabalho")
public class HorarioDeTrabalhoController {

    private final HorarioDeTrabalhoService service;
    public HorarioDeTrabalhoController(HorarioDeTrabalhoService service) {
        this.service = service;
    }

    // Criar Horario de trabalho para o barbeiro, somente administrador pode criar.
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<HorarioDeTrabalhoSaidaDto> cadastrar(@RequestBody @Valid HorarioDeTrabalhoDto dto){
        HorarioDeTrabalhoSaidaDto saida = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }

    // Atualizar Horario de trabalho do berbeiro, somente administrador pode alterar.
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<HorarioDeTrabalhoSaidaDto> atualizarInformacoes(@PathVariable Long id, @RequestBody @Valid HorarioDeTrabalhoAtualizarDto dto){
        return ResponseEntity.ok(service.atualizarHorarioDeTrabalho(id,dto));
    }

    // Excluir horario de trabalho do barbeiro, somente administrador pode excluir - No servico valido se tem agendamento para o barbeiro.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void excluir(@PathVariable Long id){
        service.excluirHorarioDeTrabalho(id);
    }


}
