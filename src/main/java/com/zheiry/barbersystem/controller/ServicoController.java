package com.zheiry.barbersystem.controller;


import com.zheiry.barbersystem.dto.servico.ServicoAtualizarDto;
import com.zheiry.barbersystem.dto.servico.ServicoDto;
import com.zheiry.barbersystem.dto.servico.ServicoSaidaDto;
import com.zheiry.barbersystem.services.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService service;
    public ServicoController(ServicoService service) {
        this.service = service;
    }



    // Cadastrar serviço - sómente administrador tem altorização
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ServicoSaidaDto> cadastrarServico(@RequestBody @Valid ServicoDto dto){
        ServicoSaidaDto saida = service.cadastrarServico(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saida);
    }

    // Lista seriços ativos
    @GetMapping("/lista/ativos")
    public ResponseEntity<List<ServicoSaidaDto>> listaServicoStatusAtivo(){
        return ResponseEntity.ok(service.listaServicoStatusAtivo());
    }

    // Lista serviços com status inativo - somente administrador tem acesso.
    @GetMapping("/lista/inativos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ServicoSaidaDto>> listaServicoStatusInativo(){
        return ResponseEntity.ok(service.listaServicoStatusInativo());
    }

    // Buscar por nome.
    @GetMapping("/nome")
    public ResponseEntity<List<ServicoSaidaDto>> buscarPorNome(@RequestParam String nome){
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    // Buscar por categoria.
//    @GetMapping("/categoria")
//    public ResponseEntity<List<ServicoSaidaDto>> buscarPorCategoria(@RequestParam String categoria){
//        return ResponseEntity.ok(service.buscarPorCategoria(categoria));
//    }

    // Buscar por ID.
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ServicoSaidaDto> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // Atualizar informaçoes - só o admistrador pode atualizar informações do servico.
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ServicoSaidaDto> atualizarServico(@PathVariable Long id, @RequestBody @Valid ServicoAtualizarDto dto){
        return ResponseEntity.ok(service.atualizarServico(id, dto));
    }

    // Exclusão lógica - desativa serviço, só o administrador pode desativar.
    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desativarServico(@PathVariable Long id){
        service.desativarServico(id);
        return ResponseEntity.noContent().build();
    }

    // Reativa serviço - só administrador pode reativar serviço.
    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> reativarServico(@PathVariable Long id){
        service.ativarServico(id);
        return ResponseEntity.noContent().build();
    }





}
