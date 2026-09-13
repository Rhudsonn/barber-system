package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.servico.ServicoAtualizarDto;
import com.zheiry.barbersystem.dto.servico.ServicoDto;
import com.zheiry.barbersystem.dto.servico.ServicoSaidaDto;
import com.zheiry.barbersystem.entities.ServicoEntity;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.DuplicateResourceException;
import com.zheiry.barbersystem.exception.ResourceNotFoundException;
import com.zheiry.barbersystem.mappers.ServicoMapper;
import com.zheiry.barbersystem.repositories.ServicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository repository;
    public ServicoService(ServicoRepository repository) {
        this.repository = repository;
    }



    // Cadastrar Serviço - só o administrador tem altorização.
    @Transactional
    public ServicoSaidaDto cadastrarServico(ServicoDto dto) throws BadRequestException {

        // Verificar se nome ja esta em uso.
        if (repository.existsByNome(dto.nome())){
            throw new DuplicateResourceException("Nome  do serviço já esta em uso!");
        }

        ServicoEntity entity = ServicoMapper.toEntity(dto);

        entity.setStatus(Status.ATIVO);

        ServicoEntity salva = repository.save(entity);

        return ServicoMapper.toSaidaDto(salva);
    }

    // Listar - Ativo, Inativo.
    public List<ServicoSaidaDto> listaServicosPorStatus(Status status){
        List<ServicoEntity> servicos = repository.findByStatus(status);
        return ServicoMapper.toSaidaDtoList(servicos);
    }

    // Lista serviços com status ativo.
    public List<ServicoSaidaDto> listaServicoStatusAtivo(){
        return listaServicosPorStatus(Status.ATIVO);
    }

    // Lista serviço com status inativo.
    public List<ServicoSaidaDto> listaServicoStatusInativo(){
        return listaServicosPorStatus(Status.INATIVO);
    }


    // Buscar por nome.
    public List<ServicoSaidaDto> buscarPorNome(String nome){
        List<ServicoEntity> servicos = repository.findByNomeContainingIgnoreCase(nome);
        return ServicoMapper.toSaidaDtoList(servicos);
    }

    // Buscar por ID.
    public ServicoSaidaDto buscarPorId(Long id){
        ServicoEntity entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado!"));
        return ServicoMapper.toSaidaDto(entity);
    }

    // Buscar por Categoria.
//    public List<ServicoSaidaDto> buscarPorCategoria(String categoria){
//        List<ServicoEntity> servicos = repository.findByCategoriaContainingIgnoreCase(categoria);
//        return ServicoMapper.toSaidaDtoList(servicos);
//    }

    // Atualizar informações.
    @Transactional
    public ServicoSaidaDto atualizarServico(Long id, ServicoAtualizarDto dto){
        // Busca o Serviço. Se não existir, lança 404.
        ServicoEntity entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado!"));

        // Valida se o nome ja esta sendo usado por outro serviço.
        if (dto.nome() != null && repository.existsByNomeAndIdNot(dto.nome(), id)){
            throw new DuplicateResourceException("Nome informado já pertence a outro produto cadastrado!");
        }

        ServicoMapper.atualizaEntity(dto, entity);

        repository.save(entity);

        return ServicoMapper.toSaidaDto(entity);
    }


    // Excluir - exclusão lógica.
    @Transactional
    public void desativarServico(Long id){
        ServicoEntity servico = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado!"));
        // Evita tentar desativar serviço que ja esta desativado
        if (servico.getStatus() == Status.INATIVO) {
            throw new BadRequestException("Este serviço já esta desativado!");
        }
        servico.setStatus(Status.INATIVO);
        repository.save(servico);
    }


    // Reativar Serviço.
    @Transactional
    public void ativarServico(Long id){
        ServicoEntity servico = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado!"));
        // Evita tentar dativar um serviço que ja esta ativo.
        if (servico.getStatus() == Status.ATIVO) {
            throw new BadRequestException("Este serviço já esta ativo!");
        }
        servico.setStatus(Status.ATIVO);
        repository.save(servico);
    }



}
