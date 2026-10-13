package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.cliente.ClienteAlteraSenhaDto;
import com.zheiry.barbersystem.dto.cliente.ClienteAtualizaDto;
import com.zheiry.barbersystem.dto.cliente.ClienteDto;
import com.zheiry.barbersystem.dto.cliente.ClienteSaidaDto;
import com.zheiry.barbersystem.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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


    //Listar clientes com status ativo.
    @GetMapping("/lista/ativo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BARBEIRO')")
    public ResponseEntity<List<ClienteSaidaDto>> listaClientesStatusAtivo(){
        return ResponseEntity.ok(service.listaClientesStatusAtivo());
    }

    //Lista clientes com status inativo.
    @GetMapping("lista/inativo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ClienteSaidaDto>> listaClientesStatusInativo(){
        return ResponseEntity.ok(service.listaClientesStatusInativo());
    }


    // Buscar cliente por Nome.
    @GetMapping("/busca/nome")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BARBEIRO')")
    public ResponseEntity<List<ClienteSaidaDto>> buscarPorNome(@RequestParam String nome){
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    // Buscar cliente por ID.
    @GetMapping("/{id}")
    public ResponseEntity<ClienteSaidaDto> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(service.burcarClientePorId(id));
    }

    // Atualizar informações.
    //Não atualizo todos os dados, pode ser atualizado os campos que quiser.
    @PatchMapping("{id}")
    @PreAuthorize("hasRole('CLIENTE') and #id == authentication.principal.id")
    public ResponseEntity<ClienteSaidaDto> atualizarInformacoes(@PathVariable Long id, @RequestBody @Valid ClienteAtualizaDto dto){
        return ResponseEntity.ok(service.atualizarInformacoesCliente(id,dto));
    }


    //Alterar senha - somente o cliente pode alterar sua senha
    @PatchMapping("/{id}/senha")
    @PreAuthorize("hasRole('CLIENTE') and #id == authentication.principal.id")
    public ResponseEntity<Void> alterarSenha(@PathVariable Long id, @RequestBody @Valid ClienteAlteraSenhaDto dto){
        service.alterarSenha(id,dto);
        return ResponseEntity.noContent().build();
    }


    // Desativar cliente - exclusão lógica
    @PatchMapping("/desativar/{id}")
    @PreAuthorize("hasRole('CLIENTE') and #id == authentication.principal.id")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // Reativar cliente - (controle do ADMINISTRADOR)
    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        service.reativarCliente(id);
        return ResponseEntity.noContent().build();
    }



}
