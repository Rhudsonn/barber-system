package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.agendamento.AgendamentoAtualizarStatusDto;
import com.zheiry.barbersystem.dto.agendamento.AgendamentoDto;
import com.zheiry.barbersystem.dto.agendamento.AgendamentoReagendarDto;
import com.zheiry.barbersystem.dto.agendamento.AgendamentoSaidaDto;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.services.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    // Criar Agendamento
    // Cliente, Barbeiro e Admin podem criar (pode restringir depois se precisar).
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> criarAgendamento(@RequestBody @Valid AgendamentoDto dto) throws BadRequestException {
        AgendamentoSaidaDto saida = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }


    // Busca agendamento por ID.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }


    // Lista agendamentos de um cliente (paginado).
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR')")
    public ResponseEntity<Page<AgendamentoSaidaDto>> listarPorCliente(
            @PathVariable Long clienteId,
            Pageable pageable) {
        return ResponseEntity.ok(service.listarPorCliente(clienteId, pageable));
    }


    // Lista agendamentos de um barbeiro (paginado).
    @GetMapping("/barbeiro/{barbeiroId}")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<Page<AgendamentoSaidaDto>> listarPorBarbeiro(
            @PathVariable Long barbeiroId,
            Pageable pageable) {
        return ResponseEntity.ok(service.listarPorBarbeiro(barbeiroId, pageable));
    }


    // Lista agendamentos de um barbeiro em uma data específica.
    @GetMapping("/barbeiro/{barbeiroId}/data")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR', 'CLIENTE')")
    public ResponseEntity<List<AgendamentoSaidaDto>> listarPorBarbeiroEData(
            @PathVariable Long barbeiroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.listarPorBarbeiroEData(barbeiroId, data));
    }


    // Lista todos os agendamentos (somente Admin).
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<AgendamentoSaidaDto>> listarTodos(Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }


    //HORÁRIOS DISPONÍVEIS
   // Retorna os horários livres de um barbeiro em uma data, considerando a duração do serviço.
    @GetMapping("/disponibilidade")
    @PreAuthorize("hasAnyRole('CLIENTE', 'BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<List<LocalTime>> listarHorariosDisponiveis(
            @RequestParam Long barbeiroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam int duracaoMinutos) {

        return ResponseEntity.ok(service.listarHorariosDisponiveis(barbeiroId, data, duracaoMinutos));
    }


    //ATUALIZAR STATUS
    // Atualiza o status de um agendamento.
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AgendamentoAtualizarStatusDto dto) {

        return ResponseEntity.ok(service.atualizarStatus(id, dto.statusAgendamento()));
    }

    // CANCELAMENTO
    // Cancelamento feito pelo Cliente. O clienteId deve ser o do usuário logado (pode ser pego do JWT depois).
    @PatchMapping("/{id}/cancelar/cliente")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<AgendamentoSaidaDto> cancelarPorCliente(
            @PathVariable Long id,
            @RequestParam Long clienteId) throws BadRequestException {

        return ResponseEntity.ok(service.cancelarPorCliente(id, clienteId));
    }

    // Cancelamento feito pelo Barbeiro.
    @PatchMapping("/{id}/cancelar/barbeiro")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> cancelarPorBarbeiro(
            @PathVariable Long id,
            @RequestParam Long barbeiroId) throws BadRequestException {

        return ResponseEntity.ok(service.cancelarPorBarbeiro(id, barbeiroId));
    }


    // REAGENDAR
    // Reagenda um agendamento para uma nova data/hora.
    @PatchMapping("/{id}/reagendar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> reagendar(
            @PathVariable Long id,
            @RequestBody @Valid AgendamentoReagendarDto dto) throws BadRequestException {

        return ResponseEntity.ok(service.reagendar(id, dto.novaDataHoraInicio()));
    }


    // FLUXO DE ATENDIMENTO (Barbeiro / Admin)
    // Marca o agendamento como EM_ANDAMENTO.
    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> iniciarAtendimento(@PathVariable Long id) {
        return ResponseEntity.ok(service.iniciarAtendimento(id));
    }

    // Marca o agendamento como CONCLUIDO.
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> finalizarAtendimento(@PathVariable Long id) {
        return ResponseEntity.ok(service.finalizarAtendimento(id));
    }


    // Marca o agendamento como NAO_COMPARECEU (cliente faltou).
    @PatchMapping("/{id}/nao-compareceu")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AgendamentoSaidaDto> marcarNaoCompareceu(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarNaoCompareceu(id));
    }
}
