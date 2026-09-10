package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.cliente.ClienteDto;
import com.zheiry.barbersystem.dto.cliente.ClienteSaidaDto;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.exception.DuplicateResourceException;
import com.zheiry.barbersystem.mappers.ClienteMapper;
import com.zheiry.barbersystem.repositories.ClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClienteSaidaDto cadastroCliente(ClienteDto dto) throws BadRequestException{

        // Verifica se o e-mail informado já está em uso
        if (repository.existsByEmail(dto.email())){
            throw new DuplicateResourceException("E-mail já existente!");
        }
        // Verifica se o telefone informado já pertence a outro barbeiro.
        if (repository.existsByTelefone(dto.telefone())){
            throw new DuplicateResourceException("Telefone já existente!");
        }

        // Valida se a senha bate com o campo de confirmação de senha.
        if (!dto.senha().equals(dto.confirmaSenha())){
            throw new BadRequestException("Senha não condiz com a confirmação de senha!");
        }

        // MAPEAMENTO: Converte os dados do DTO de entrada para a Entidade de banco de dados
        ClienteEntity entity = ClienteMapper.toEntity(dto);

        // SEGURANÇA: Criptografa a senha antes de salvar no banco
        String senhaCriptografada = passwordEncoder.encode(dto.senha());
        entity.setSenha(senhaCriptografada);

        // REGRA DE NEGÓCIO: Define por padrão o perfil do usuário como Cliente
        entity.setNivelAcesso(NivelAcesso.CLIENTE);

        // REGRA DE NEGÓCIO: Define por padrão o perfil do usuário como ATIVO
        entity.setStatus(Status.ATIVO);

        // PERSISTÊNCIA: Grava o novo barbeiro no banco de dados e recupera a entidade salva (com ID gerado)
        ClienteEntity salva = repository.save(entity);

        // RETORNO: Converte a entidade salva para o DTO de saída e o retorna para a camada superior (Controller)
        return ClienteMapper.toSaidaDto(salva);
    }


    // Médoto que lista clientes por status
    public List<ClienteSaidaDto> listaClientesPorStatus(Status status){
        List<ClienteEntity> clientes = repository.findByStatus(status);
        return ClienteMapper.toSaidaDtoList(clientes);
    }

    // Listar clientes com status --- ativo
    public List<ClienteSaidaDto> listaClientesStatusAtivo(){
        return listaClientesPorStatus(Status.ATIVO);
    }

    // Listar clientes com status --- inativo
    public List<ClienteSaidaDto> listaClientesStatusInativo(){
        return listaClientesPorStatus(Status.INATIVO);
    }


}
