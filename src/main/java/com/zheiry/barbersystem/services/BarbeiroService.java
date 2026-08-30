package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dtos.BarbeiroDto;
import com.zheiry.barbersystem.dtos.BarbeiroSaidaDto;
import com.zheiry.barbersystem.entities.BarbeiroEntity;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.exception.DuplicateResourceException;
import com.zheiry.barbersystem.mappers.BarbeiroMapper;
import com.zheiry.barbersystem.repositories.BarbeiroRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class BarbeiroService {

    private final BarbeiroRepository repository;

    public BarbeiroService(BarbeiroRepository repository) {
        this.repository = repository;
    }


    //Realiza o cadastro de um novo barbeiro no sistema após validar todas as regras de negócio.
    // @param dto Objeto com os dados de entrada preenchidos no formulário (nome, e-mail, senhas, etc.).
    // @return BarbeiroSaidaDto Objeto de resposta sem dados sensíveis, pronto para a interface web.
    // @throws BadRequestException Lançada quando a senha e a confirmação de senha não coincidem.
    // @throws DuplicateResourceException Lançada quando o e-mail ou o telefone já constam no banco.
    //Método responsável por adicionar/cadastrar um barbeiro
    public BarbeiroSaidaDto cadastroBarbeiro(BarbeiroDto dto) throws BadRequestException {

        // Verifica se o e-mail informado já está em uso
        if(repository.existsByEmail(dto.email())){
            throw new DuplicateResourceException("E-mail já existente!");
        }

        // Verifica se o telefone informado já pertence a outro barbeiro.
        if (repository.existsByTelefone(dto.telefone())) {
            throw new DuplicateResourceException("Telefone já existente!");
        }

        // Valida se a senha bate com o campo de confirmação de senha.
        // Depois vou ter que alterar, porque vou criptografar a senha.
        if (!dto.senha().equals(dto.confirmaSenha())){
            throw new BadRequestException("Senha não condiz com a confirmação de senha!");
        }

        // MAPEAMENTO: Converte os dados do DTO de entrada para a Entidade de banco de dados
        BarbeiroEntity entity = BarbeiroMapper.toEntity(dto);

        // REGRA DE NEGÓCIO: Define por padrão o perfil do usuário como BARBEIRO
        entity.setNivelAcesso(NivelAcesso.BARBEIRO);

        // PERSISTÊNCIA: Grava o novo barbeiro no banco de dados e recupera a entidade salva (com ID gerado)
        BarbeiroEntity saved = repository.save(entity);

        // RETORNO: Converte a entidade salva para o DTO de saída e o retorna para a camada superior (Controller)
        return BarbeiroMapper.toDto(saved);
    }

}
