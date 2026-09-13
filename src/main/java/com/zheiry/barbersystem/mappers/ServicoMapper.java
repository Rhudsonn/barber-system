package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.servico.ServicoAtualizarDto;
import com.zheiry.barbersystem.dto.servico.ServicoDto;
import com.zheiry.barbersystem.dto.servico.ServicoSaidaDto;
import com.zheiry.barbersystem.entities.ServicoEntity;

import java.util.List;

public class ServicoMapper {

    private ServicoMapper(){}

    // Dto - Entity
    // Converte os dados recebidos do formulário (ServiçooDto) para um objeto de banco de dados (ServicoEntity).
    // @param dto Objeto com os dados enviados pela interface web.
    // @return Entidade pronta para ser salva no banco via Repository.
    public static ServicoEntity toEntity(ServicoDto dto){

        if(dto == null) return null;

        ServicoEntity entity = new ServicoEntity();

        entity.setNome(dto.nome());
        entity.setDescricao(dto.descricao());
        entity.setCategoria(dto.categoria());
        entity.setPreco(dto.preco());
        entity.setDuracaoEmMinutos(dto.duracaoEmMinutos());
        return entity;
    }


    // Entity - Dto
    // Converte a Entidade vinda do banco de dados (ServicoEntity) para o DTO (ServicoSaidaDto)
    // que será exibido na tela HTML/Thymeleaf.
    // @param entity Objeto recuperado do banco de dados.
    // @return DTO formatado para a visualização na camada de apresentação.
    public static ServicoSaidaDto toSaidaDto(ServicoEntity entity){
        if(entity == null) return null;
        return new ServicoSaidaDto(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getCategoria(),
                entity.getPreco(),
                entity.getDuracaoEmMinutos()
        );
    }



    //Converte uma lista de Entities em uma lista de DTOs de saída.
    //Reaproveita o método individual acima, evitando duplicação
    //@param entities lista vinda do repositório
    //@return lista de DTOs prontos para resposta da APIde lógica de mapeamento.
    public static List<ServicoSaidaDto> toSaidaDtoList(List<ServicoEntity> entities){
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(ServicoMapper::toSaidaDto)
                .toList();
    }



    //Aplica os dados do DTO de atualização em cima de uma Entity
    public static void atualizaEntity(ServicoAtualizarDto dto, ServicoEntity entity) {

        if (dto == null ||  entity == null) return;

        if (dto.nome() != null){
            entity.setNome(dto.nome());
        }
        if (dto.descrica() != null){
            entity.setDescricao(dto.descrica());
        }
        if (dto.categoria() != null){
            entity.setCategoria(dto.categoria());
        }
        if (dto.preco() != null){
            entity.setPreco(dto.preco());
        }
        if (dto.duracaoEmMinutos() != null){
            entity.setDuracaoEmMinutos(dto.duracaoEmMinutos());
        }



    }

}
