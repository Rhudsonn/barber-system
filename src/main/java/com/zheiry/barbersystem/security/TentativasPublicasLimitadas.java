package com.zheiry.barbersystem.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;

/** Limites temporários por processo para os endpoints públicos mais expostos. */
@Service
public class TentativasPublicasLimitadas {

    private static final int LOGIN_IP_LIMITE = 60;
    private static final Duration LOGIN_IP_JANELA = Duration.ofMinutes(15);
    private static final int LOGIN_CONTA_LIMITE = 20;
    private static final Duration LOGIN_CONTA_JANELA = Duration.ofMinutes(15);
    private static final int CADASTRO_IP_LIMITE = 10;
    private static final Duration CADASTRO_IP_JANELA = Duration.ofHours(1);
    private static final int RECUPERACAO_IP_LIMITE = 10;
    private static final Duration RECUPERACAO_IP_JANELA = Duration.ofHours(1);
    private static final int RECUPERACAO_CONTA_LIMITE = 3;
    private static final Duration RECUPERACAO_CONTA_JANELA = Duration.ofHours(1);
    private static final int CAPACIDADE_MAXIMA = 100_000;

    private final Clock clock;
    private final byte[] chaveHashConta = new byte[32];
    private final Map<String, Janela> janelas = new HashMap<>();
    private long operacoes;

    public TentativasPublicasLimitadas(Clock clock) {
        this.clock = clock;
        new SecureRandom().nextBytes(chaveHashConta);
    }

    public synchronized void verificarLoginPorIp(String ip) {
        consumir("login:ip:" + ip, LOGIN_IP_LIMITE, LOGIN_IP_JANELA);
    }

    public synchronized void verificarLoginPorConta(String email) {
        consumir("login:conta:" + hash(normalizar(email)), LOGIN_CONTA_LIMITE, LOGIN_CONTA_JANELA);
    }

    public synchronized void verificarCadastroPorIp(String ip) {
        consumir("cadastro:ip:" + ip, CADASTRO_IP_LIMITE, CADASTRO_IP_JANELA);
    }

    public synchronized void verificarRecuperacaoPorIp(String ip) {
        consumir("recuperacao:ip:" + ip, RECUPERACAO_IP_LIMITE, RECUPERACAO_IP_JANELA);
    }

    public synchronized void verificarRecuperacaoPorConta(String email) {
        consumir("recuperacao:conta:" + hash(normalizar(email)), RECUPERACAO_CONTA_LIMITE,
                RECUPERACAO_CONTA_JANELA);
    }

    private void consumir(String chave, int limite, Duration duracao) {
        long agora = clock.millis();
        Janela janela = janelas.get(chave);
        if (janela != null && agora >= janela.expiraEm) {
            janelas.remove(chave);
            janela = null;
        }

        if (janela == null) {
            if (janelas.size() >= CAPACIDADE_MAXIMA) {
                removerExpiradas(agora);
            }
            if (janelas.size() >= CAPACIDADE_MAXIMA) {
                long proximaLiberacao = janelas.values().stream().mapToLong(item -> item.expiraEm).min()
                        .orElse(agora + duracao.toMillis());
                throw new LimiteTentativasExcedidoException(Duration.ofMillis(Math.max(1, proximaLiberacao - agora)));
            }
            janela = new Janela(agora + duracao.toMillis());
            janelas.put(chave, janela);
        }

        if (janela.tentativas >= limite) {
            throw new LimiteTentativasExcedidoException(Duration.ofMillis(Math.max(1, janela.expiraEm - agora)));
        }
        janela.tentativas++;

        // Limpeza amortizada para não percorrer o mapa em cada requisição.
        if (++operacoes % 256 == 0) {
            removerExpiradas(agora);
        }
    }

    private void removerExpiradas(long agora) {
        Iterator<Janela> iterator = janelas.values().iterator();
        while (iterator.hasNext()) {
            if (agora >= iterator.next().expiraEm) iterator.remove();
        }
    }

    private static String normalizar(String conta) {
        String valor = conta == null ? "" : conta.strip().toLowerCase(java.util.Locale.ROOT);
        // Evita chaves arbitrariamente grandes sem colapsar contas comuns.
        return valor.length() <= 320 ? valor : valor.substring(0, 320);
    }

    private String hash(String valor) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(chaveHashConta, "HmacSHA256"));
            byte[] digest = mac.doFinal(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HMAC-SHA256 indisponível", ex);
        }
    }

    private static final class Janela {
        private final long expiraEm;
        private int tentativas;

        private Janela(long expiraEm) {
            this.expiraEm = expiraEm;
        }
    }
}
