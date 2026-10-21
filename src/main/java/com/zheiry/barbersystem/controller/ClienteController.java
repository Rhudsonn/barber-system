package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.config.springSecurity.UsuarioAutenticado;
import com.zheiry.barbersystem.dto.cliente.ClienteAlteraSenhaDto;
import com.zheiry.barbersystem.dto.cliente.ClienteAtualizaDto;
import com.zheiry.barbersystem.dto.cliente.ClienteDto;
import com.zheiry.barbersystem.dto.cliente.ClienteSaidaDto;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;
    public ClienteController(ClienteService service) {this.service = service;}

    // Cadastro de cliente - cliente se cadastra - publico (opção de cadastro, ficara na tela de login).
    @PostMapping
    public ResponseEntity<ClienteSaidaDto> cadastrarCliente(@RequestBody @Valid ClienteDto dto){
        ClienteSaidaDto saida = service.cadastroCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }


    // Lista de clientes ATIVOS (DTO resumido)
    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<List<?>> listarAtivos(Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (isAdmin) {
            return ResponseEntity.status(HttpStatus.OK).body(service.listarPorStatusAdmin(Status.ATIVO));
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.listarAtivosParaBarbeiro());
    }


    // Clientes INATIVOS (só admin)
    @GetMapping("/inativos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ClienteSaidaDto>> listarInativos() {
        return ResponseEntity.status(HttpStatus.OK).body(service.listarPorStatusAdmin(Status.INATIVO));
    }


    // Buscar cliente por Nome.
    @GetMapping("/busca/nome")
    public ResponseEntity<List<?>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.status(HttpStatus.OK).body(service.buscarPorNome(nome));
    }



    // Atualizar informações.
    //Não atualizo todos os dados, pode ser atualizado os campos que quiser.
    @PatchMapping("/meu-perfil/{id}")
    @PreAuthorize("hasRole('CLIENTE') and #id == authentication.principal.id")
    public ResponseEntity<ClienteSaidaDto> atualizarInformacoes(@PathVariable Long id, @RequestBody @Valid ClienteAtualizaDto dto){
        return ResponseEntity.status(HttpStatus.OK).body(service.atualizarInformacoesCliente(id,dto));
    }


    //Alterar senha - somente o cliente pode alterar sua senha
    @PatchMapping("/meu-perfil/senha/{id}")
    @PreAuthorize("hasRole('CLIENTE') and #id == authentication.principal.id")
    public ResponseEntity<Void> alterarSenha(@PathVariable Long id, @RequestBody @Valid ClienteAlteraSenhaDto dto){
        service.alterarSenha(id,dto);
        return ResponseEntity.noContent().build();
    }


    // Desativar cliente - exclusão lógica
    @PatchMapping("/delete/{id}")
    @PreAuthorize("hasRole('CLIENTE') and #id == authentication.principal.id")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // Reativar cliente - (controle do ADMINISTRADOR)
    @PatchMapping("/reativar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        service.reativarCliente(id);
        return ResponseEntity.noContent().build();
    }



}
