package com.zheiry.barbersystem.mappers;

import com.zheiry.barbersystem.dto.notificacao.NotificacaoSaidaDto;
import com.zheiry.barbersystem.dto.notificacao.PreferenciaNotificacaoAtualizaDto;
import com.zheiry.barbersystem.dto.notificacao.PreferenciaNotificacaoSaidaDto;
import com.zheiry.barbersystem.entities.ClienteEntity;
import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.CanalNotificacao;

import java.util.List;

public final class NotificacaoMapper {
    private NotificacaoMapper() { }

    public static PreferenciaNotificacaoSaidaDto paraPreferencia(ClienteEntity cliente) {
        // Old preference values remain in the database, but the application now supports e-mail only.
        return new PreferenciaNotificacaoSaidaDto(CanalNotificacao.EMAIL, cliente.isNotificacoesAutorizadas());
    }

    public static void atualizarPreferencia(PreferenciaNotificacaoAtualizaDto dto, ClienteEntity cliente) {
        cliente.setCanalNotificacao(CanalNotificacao.EMAIL.name());
        cliente.setNotificacoesAutorizadas(dto.autorizada());
    }

    public static NotificacaoSaidaDto paraSaida(NotificacaoEntity entity) {
        return new NotificacaoSaidaDto(entity.getId(), entity.getAgendamentoId(), entity.getTipo(),
                entity.getCanal(), entity.getStatus(), entity.getProgramadaPara(), entity.getTentadaEm(),
                entity.getEnviadaEm(), entity.getQuantidadeTentativas(), entity.getErroSeguro());
    }

    public static List<NotificacaoSaidaDto> paraSaida(List<NotificacaoEntity> entities) {
        return entities.stream().map(NotificacaoMapper::paraSaida).toList();
    }
}
