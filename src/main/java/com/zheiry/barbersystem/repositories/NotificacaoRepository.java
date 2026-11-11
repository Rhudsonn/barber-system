package com.zheiry.barbersystem.repositories;

import com.zheiry.barbersystem.entities.NotificacaoEntity;
import com.zheiry.barbersystem.enums.StatusNotificacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<NotificacaoEntity, Long> {
    boolean existsByChaveIdempotencia(String chaveIdempotencia);
    Optional<NotificacaoEntity> findByChaveIdempotencia(String chaveIdempotencia);
    List<NotificacaoEntity> findByClienteIdAndStatus(Long clienteId, StatusNotificacao status);
    List<NotificacaoEntity> findByClienteIdOrderByCriadaEmDesc(Long clienteId, Pageable pageable);

    @Query("select n.id from NotificacaoEntity n where (n.status = :pendente and n.programadaPara <= :agora) " +
            "or (n.status = :processando and n.tentadaEm <= :limiteLease) order by n.programadaPara")
    List<Long> buscarIdsDevidas(@Param("pendente") StatusNotificacao pendente,
                               @Param("processando") StatusNotificacao processando,
                               @Param("agora") LocalDateTime agora,
                               @Param("limiteLease") LocalDateTime limiteLease,
                               Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificacaoEntity n set n.status = :processando, n.tentadaEm = :agora, " +
            "n.quantidadeTentativas = n.quantidadeTentativas + 1 where n.id = :id and " +
            "((n.status = :pendente and n.programadaPara <= :agora) or " +
            "(n.status = :processando and n.tentadaEm <= :limiteLease))")
    int reservar(@Param("id") Long id,
                 @Param("pendente") StatusNotificacao pendente,
                 @Param("processando") StatusNotificacao processando,
                 @Param("agora") LocalDateTime agora,
                 @Param("limiteLease") LocalDateTime limiteLease);

    @Modifying
    @Query("update NotificacaoEntity n set n.status = :cancelada, n.erroSeguro = :motivo " +
            "where n.agendamentoId = :agendamentoId and n.status = :pendente and n.tipo in :tipos")
    int cancelarPendentes(@Param("agendamentoId") Long agendamentoId,
                          @Param("pendente") StatusNotificacao pendente,
                          @Param("cancelada") StatusNotificacao cancelada,
                          @Param("tipos") List<com.zheiry.barbersystem.enums.TipoNotificacao> tipos,
                          @Param("motivo") String motivo);
}
