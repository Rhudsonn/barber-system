package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dtos.BarbeiroDto;
import com.zheiry.barbersystem.dtos.BarbeiroSaidaDto;
import com.zheiry.barbersystem.entities.BarbeiroEntity;

public class BarbeiroMapper {

    // Construtor privado: impede que a classe seja instanciada com 'new BarbeiroMapper()',
    // já que todos os métodos utilitários são estáticos
    private BarbeiroMapper(){}

    // Dto - Entity
    // Converte os dados recebidos do formulário (BarbeiroDto) para um objeto de banco de dados (BarbeiroEntity).
    // @param dto Objeto com os dados enviados pela interface web.
    // @return Entidade pronta para ser salva no banco via Repository.
    public static BarbeiroEntity toEntity(BarbeiroDto dto){

        // Proteção contra NullPointerException: se o DTO for nulo, retorna nulo diretamente.
        if(dto == null) return null;

        BarbeiroEntity entity = new BarbeiroEntity();

        entity.setNome(dto.nome());
        entity.setDataNascimento(dto.dataNascimento());
        entity.setTelefone(dto.telefone());
        entity.setEspecialidade(dto.especialidade());
        entity.setEmail(dto.email());

        //Falta emplementa criptografia da senha.......
        // Atribui a senha (na camada de Service, você criptografará essa senha antes do save)
        entity.setSenha(dto.senha());

        return entity;
    }


    // Entity - Dto
    // Converte a Entidade vinda do banco de dados (BarbeiroEntity) para o DTO (BarbeiroDto)
    // que será exibido na tela HTML/Thymeleaf.
    // @param entity Objeto recuperado do banco de dados.
    // @return DTO formatado para a visualização na camada de apresentação.
    public static BarbeiroSaidaDto toDto(BarbeiroEntity entity){

        // Proteção contra NullPointerException: se a entidade for nula, retorna nulo.
        if(entity == null) return null;

        return new BarbeiroSaidaDto(
                entity.getId(), // Retorna o ID necessário para links do Thymeleaf
                entity.getNome(),
                entity.getDataNascimento(),
                entity.getTelefone(),
                entity.getEspecialidade(),
                entity.getEmail()
        );
    }

}
