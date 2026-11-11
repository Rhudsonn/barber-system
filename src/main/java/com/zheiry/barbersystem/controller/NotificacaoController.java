package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.notificacao.EstadoIntegracoesNotificacaoDto;
import com.zheiry.barbersystem.dto.notificacao.NotificacaoSaidaDto;
import com.zheiry.barbersystem.dto.notificacao.PreferenciaNotificacaoAtualizaDto;
import com.zheiry.barbersystem.dto.notificacao.PreferenciaNotificacaoSaidaDto;
import com.zheiry.barbersystem.services.NotificacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificacaoController {
    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) { this.service = service; }

    @GetMapping("/clientes/meu-perfil/notificacoes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PreferenciaNotificacaoSaidaDto> consultarPreferencia() {
        return ResponseEntity.ok(service.consultarPreferencia());
    }

    @PutMapping("/clientes/meu-perfil/notificacoes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PreferenciaNotificacaoSaidaDto> atualizarPreferencia(
            @RequestBody @Valid PreferenciaNotificacaoAtualizaDto dto) {
        return ResponseEntity.ok(service.atualizarPreferencia(dto));
    }

    @GetMapping("/clientes/meu-perfil/notificacoes/historico")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<NotificacaoSaidaDto>> historicoDoCliente() {
        return ResponseEntity.ok(service.historicoDoCliente());
    }

    @GetMapping("/notificacoes/integracoes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EstadoIntegracoesNotificacaoDto> estadoIntegracoes() {
        return ResponseEntity.ok(service.estadoIntegracoes());
    }
}
