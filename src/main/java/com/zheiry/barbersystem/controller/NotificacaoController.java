package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.notificacao.EstadoIntegracoesNotificacaoDto;
import com.zheiry.barbersystem.dto.notificacao.NotificacaoSaidaDto;
import com.zheiry.barbersystem.services.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificacaoController {
    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) { this.service = service; }

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
