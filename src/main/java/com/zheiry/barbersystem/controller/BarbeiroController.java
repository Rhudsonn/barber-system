package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.dto.barbeiro.BarbeiroAlteraSenhaDto;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroAtualizaDto;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroDto;
import com.zheiry.barbersystem.dto.barbeiro.BarbeiroSaidaDto;
import com.zheiry.barbersystem.services.BarbeiroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbeiros")
public class BarbeiroController {

    private final BarbeiroService service;
    public BarbeiroController(BarbeiroService service) {
        this.service = service;
    }


    // Cadastrar barbeiro - sómente administrador tem altorização.
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<BarbeiroSaidaDto> cadastrar(@RequestBody @Valid BarbeiroDto dto) {
        BarbeiroSaidaDto saida = service.cadastroBarbeiro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }


    // Lista de barbeiros ATIVOS.
    @GetMapping("/lista/ativos")
    public ResponseEntity<List<BarbeiroSaidaDto>> listaBarbeirosAtivos(){
        return ResponseEntity.ok(service.listaBarbeirosAtivos());
    }

    // Lista de barbeiros INATIVOS - somente administrador tem altorização.
    @GetMapping("/lista/inativos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<BarbeiroSaidaDto>> listaBarbeirosInativos(){
        return ResponseEntity.ok(service.listaBarbeirosInativos());
    }

    // Buscar barbeiro por nome.
    @GetMapping
    public ResponseEntity<List<BarbeiroSaidaDto>> buscarPorNome(@RequestParam String nome){
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    // Buscar barbeiro por ID.
    @GetMapping("/{id}")
    public ResponseEntity<BarbeiroSaidaDto> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(service.buscarBarbeiroPorId(id));
    }


    // Atualizar informações.
    //Não atualizo todos os dados, pode ser atualizado os campos que quiser.
    @PatchMapping("/{id}")
    public ResponseEntity<BarbeiroSaidaDto> atualizarInformacoes(@PathVariable Long id, @RequestBody @Valid BarbeiroAtualizaDto dto) {
        return ResponseEntity.ok(service.atualizarInformacoesBarbeiro(id, dto));
    }


    // Alterar senha - somente o barbeiro tem altorização
    @PatchMapping("/{id}/senha")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> alterarSenha(@PathVariable Long id, @RequestBody @Valid BarbeiroAlteraSenhaDto dto) {
        service.alterarSenha(id, dto);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    // Desativar barbeiro exclusao lógica - somente administrador tem altorização
    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desativarBarbeiro(@PathVariable Long id){
        service.desativarBarbeiro(id);
        return ResponseEntity.noContent().build();  //HTTP 204
    }

    // Reativar barbeiro - sómente administrador tem altorização.
    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> reativarBarbeiro(@PathVariable Long id){
        service.reativarBarbeiro(id);
        return ResponseEntity.noContent().build();  //HTTP 204
    }






}
