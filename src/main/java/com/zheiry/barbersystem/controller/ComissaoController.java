package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.comissao.RelatorioComissoesAdministradorDto;
import com.zheiry.barbersystem.dto.comissao.RelatorioComissoesBarbeiroDto;
import com.zheiry.barbersystem.dto.comissao.RelatorioPeriodoComissoesAdministradorDto;
import com.zheiry.barbersystem.dto.comissao.RelatorioPeriodoComissoesBarbeiroDto;
import com.zheiry.barbersystem.services.ComissaoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/comissoes")
public class ComissaoController {

    private final ComissaoService service;

    public ComissaoController(ComissaoService service) {
        this.service = service;
    }

    @GetMapping("/relatorio-diario/administrador")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RelatorioComissoesAdministradorDto> relatorioAdministrador(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Long barbeiroId) {
        return ResponseEntity.ok(service.relatorioAdministrador(data, barbeiroId));
    }

    @GetMapping("/relatorio-diario/meu")
    @PreAuthorize("hasRole('BARBEIRO')")
    public ResponseEntity<RelatorioComissoesBarbeiroDto> relatorioDoBarbeiro(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.relatorioDoBarbeiro(data));
    }

    @GetMapping("/relatorio-periodo/administrador")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RelatorioPeriodoComissoesAdministradorDto> relatorioAdministradorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long barbeiroId) {
        return ResponseEntity.ok(service.relatorioAdministradorPeriodo(inicio, fim, barbeiroId));
    }

    @GetMapping("/relatorio-periodo/meu")
    @PreAuthorize("hasRole('BARBEIRO')")
    public ResponseEntity<RelatorioPeriodoComissoesBarbeiroDto> relatorioDoBarbeiroPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.relatorioDoBarbeiroPeriodo(inicio, fim));
    }

}
