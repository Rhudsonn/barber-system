package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.cliente.ClienteAtualizaDto;
import com.zheiry.barbersystem.dto.cliente.ClienteDto;
import com.zheiry.barbersystem.dto.cliente.ClienteSaidaDto;
import com.zheiry.barbersystem.entities.ClienteEntity;

import java.util.List;

public class ClienteMapper {

    // Construtor privado: impede que a classe seja instanciada com 'new ClienteMapper()',
    // já que todos os métodos utilitários são estáticos
    public ClienteMapper() {
    }

    // Dto - Entity
    // Converte os dados recebidos do formulário (ClienteDto) para um objeto de banco de dados (ClienteEntity).
    // @param dto Objeto com os dados enviados pela interface web.
    // @return Entidade pronta para ser salva no banco via Repository.
    public static ClienteEntity toEntity(ClienteDto dto) {

        // Proteção contra NullPointerException: se o DTO for nulo, retorna nulo diretamente.
        if (dto == null) return null;

        ClienteEntity entity = new ClienteEntity();

        entity.setNome(dto.nome());
        entity.setDataNascimento(dto.dataNascimento());
        entity.setTelefone(dto.telefone());
        entity.setEmail(dto.email());

        // entity.setSenha(...) -> propositalmente NÃO feito aqui.
        // O Service deve fazer: entity.setSenha(passwordEncoder.encode(dto.getSenha()));

        return entity;
    }


    // Entity - Dto
    // Converte a Entidade vinda do banco de dados (ClienteEntity) para o DTO (ClienteSaidaDto)
    // que será exibido na tela HTML/Thymeleaf.
    // @param entity Objeto recuperado do banco de dados.
    // @return DTO formatado para a visualização na camada de apresentação.
    public static ClienteSaidaDto toSaidaDto(ClienteEntity entity) {

        // Proteção contra NullPointerException: se a entidade for nula, retorna nulo.
        if (entity == null) return null;

        return new ClienteSaidaDto(
                entity.getId(),      // Retorna o ID necessário para links do Thymeleaf
                entity.getNome(),
                entity.getDataNascimento(),
                entity.getTelefone(),
                entity.getEmail(),
                entity.getStatus()
        );

    }

    //Converte uma lista de Entities em uma lista de DTOs de saída.
    //Reaproveita o método individual acima, evitando duplicação
    //@param entities lista vinda do repositório
    //@return lista de DTOs prontos para resposta da APIde lógica de mapeamento.
    public static List<ClienteSaidaDto> toSaidaDtoList(List<ClienteEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(ClienteMapper::toSaidaDto)
                .toList();
    }


    //Aplica os dados do DTO de atualização em cima de uma Entity
    public static void atualizarEntity(ClienteAtualizaDto dto, ClienteEntity entity) {

        if (dto == null || entity == null) {
            return;
        }

        entity.setNome(dto.nome());
        entity.setDataNascimento(dto.dataNascimento());
        entity.setTelefone(dto.telefone());
        entity.setEmail(dto.email());
    }


    // ---------- Sobre ClienteAlteraSenhaDto ----------
    // Propositalmente não existe um método aqui tipo "alterarSenha(dto, entity)".
    // Troca de senha envolve:
    //   1. Validar a senha atual (comparar hash) -> regra de negócio/segurança
    //   2. Gerar hash da nova senha -> depende do PasswordEncoder
    //   3. Possivelmente invalidar sessões/tokens antigos
    // Nada disso é "mapeamento de campos", e sim lógica de negócio.
    // Por isso essa responsabilidade fica inteiramente no ClienteService,
    // e não neste Mapper.
}
