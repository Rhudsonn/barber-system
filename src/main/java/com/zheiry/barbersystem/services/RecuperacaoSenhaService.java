package com.zheiry.barbersystem.services;

import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaRedefinirDto;
import com.zheiry.barbersystem.dto.recuperacaoSenha.RecuperacaoSenhaSaidaDto;
import com.zheiry.barbersystem.entities.*;
import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import com.zheiry.barbersystem.exception.BadRequestException;
import com.zheiry.barbersystem.repositories.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class RecuperacaoSenhaService {

    private static final Logger logger = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private static final String MENSAGEM_GENERICA =
            "Se o e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha.";
    private static final String MENSAGEM_TOKEN_INVALIDO = "Token de recuperação inválido ou expirado.";

    private final RecuperacaoSenhaRepository recuperacaoRepository;
    private final AdministradorRepository administradorRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final ClienteRepository clienteRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String urlRedefinicao;
    private final String emailRemetente;
    private final long validadeMinutos;
    private final Clock relogio;

    public RecuperacaoSenhaService(RecuperacaoSenhaRepository recuperacaoRepository,
                                   AdministradorRepository administradorRepository,
                                   BarbeiroRepository barbeiroRepository,
                                   ClienteRepository clienteRepository,
                                   org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                                   JavaMailSender mailSender,
                                   @Value("${app.password-reset-url}") String urlRedefinicao,
                                   @Value("${app.password-reset-from:no-reply@barbearia.local}") String emailRemetente,
                                   @Value("${app.password-reset-expiration-minutes:15}") long validadeMinutos,
                                   Clock relogio) {
        this.recuperacaoRepository = recuperacaoRepository;
        this.administradorRepository = administradorRepository;
        this.barbeiroRepository = barbeiroRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.urlRedefinicao = urlRedefinicao;
        this.emailRemetente = emailRemetente;
        this.validadeMinutos = validadeMinutos;
        this.relogio = relogio;
        if (validadeMinutos <= 0) {
            throw new IllegalArgumentException("A validade do token de recuperação deve ser maior que zero.");
        }
    }

    @Transactional
    public RecuperacaoSenhaSaidaDto solicitar(String emailInformado) {
        String email = emailInformado.trim();
        recuperacaoRepository.deleteByEmailIgnoreCase(email);

        Optional<ContaRecuperacao> conta = localizarConta(email);
        if (conta.isPresent() && conta.get().status() == Status.ATIVO) {
            emitirToken(conta.get());
        }
        return new RecuperacaoSenhaSaidaDto(MENSAGEM_GENERICA);
    }

    @Transactional
    public void redefinir(RecuperacaoSenhaRedefinirDto dto) {
        RecuperacaoSenhaEntity recuperacao = recuperacaoRepository.findByTokenHash(hashToken(dto.token()))
                .orElseThrow(() -> new BadRequestException(MENSAGEM_TOKEN_INVALIDO));

        if (!recuperacao.getExpiraEm().isAfter(LocalDateTime.now(relogio))) {
            throw new BadRequestException(MENSAGEM_TOKEN_INVALIDO);
        }
        if (!dto.novaSenha().equals(dto.confirmaNovaSenha())) {
            throw new BadRequestException("Nova senha e confirmação não conferem.");
        }

        String senhaAtual = localizarSenha(recuperacao)
                .orElseThrow(() -> new BadRequestException(MENSAGEM_TOKEN_INVALIDO));
        if (passwordEncoder.matches(dto.novaSenha(), senhaAtual)) {
            throw new BadRequestException("A nova senha deve ser diferente da senha atual.");
        }

        salvarNovaSenha(recuperacao, passwordEncoder.encode(dto.novaSenha()));
        recuperacaoRepository.deleteByEmailIgnoreCase(recuperacao.getEmail());
    }

    private Optional<ContaRecuperacao> localizarConta(String email) {
        Optional<AdministradorEntity> administrador = administradorRepository.findByEmailIgnoreCase(email);
        if (administrador.isPresent()) {
            AdministradorEntity conta = administrador.get();
            return Optional.of(new ContaRecuperacao(conta.getId(), conta.getEmail(), NivelAcesso.ADMINISTRADOR, conta.getStatus()));
        }
        Optional<BarbeiroEntity> barbeiro = barbeiroRepository.findByEmailIgnoreCase(email);
        if (barbeiro.isPresent()) {
            BarbeiroEntity conta = barbeiro.get();
            return Optional.of(new ContaRecuperacao(conta.getId(), conta.getEmail(), NivelAcesso.BARBEIRO, conta.getStatus()));
        }
        return clienteRepository.findByEmailIgnoreCase(email)
                .map(conta -> new ContaRecuperacao(conta.getId(), conta.getEmail(), NivelAcesso.CLIENTE, conta.getStatus()));
    }

    private void emitirToken(ContaRecuperacao conta) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RecuperacaoSenhaEntity recuperacao = new RecuperacaoSenhaEntity();
        recuperacao.setEmail(conta.email());
        recuperacao.setUsuarioId(conta.id());
        recuperacao.setNivelAcesso(conta.nivelAcesso());
        recuperacao.setTokenHash(hashToken(token));
        recuperacao.setExpiraEm(LocalDateTime.now(relogio).plusMinutes(validadeMinutos));
        recuperacaoRepository.save(recuperacao);

        String link = UriComponentsBuilder.fromUriString(urlRedefinicao)
                .queryParam("token", token).build().encode().toUriString();
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(emailRemetente);
        mensagem.setTo(conta.email());
        mensagem.setSubject("Redefinição de senha");
        mensagem.setText("Use o link a seguir para redefinir sua senha. Ele expira em "
                + validadeMinutos + " minutos e só pode ser usado uma vez:\n\n" + link);

        try {
            mailSender.send(mensagem);
        } catch (MailException e) {
            recuperacaoRepository.delete(recuperacao);
            logger.warn("Não foi possível enviar o e-mail de recuperação de senha.");
        }
    }

    private Optional<String> localizarSenha(RecuperacaoSenhaEntity recuperacao) {
        return switch (recuperacao.getNivelAcesso()) {
            case ADMINISTRADOR -> administradorRepository.findById(recuperacao.getUsuarioId()).map(AdministradorEntity::getSenha);
            case BARBEIRO -> barbeiroRepository.findById(recuperacao.getUsuarioId()).map(BarbeiroEntity::getSenha);
            case CLIENTE -> clienteRepository.findById(recuperacao.getUsuarioId()).map(ClienteEntity::getSenha);
        };
    }

    private void salvarNovaSenha(RecuperacaoSenhaEntity recuperacao, String senha) {
        switch (recuperacao.getNivelAcesso()) {
            case ADMINISTRADOR -> administradorRepository.findById(recuperacao.getUsuarioId())
                    .filter(conta -> conta.getStatus() == Status.ATIVO)
                    .ifPresentOrElse(conta -> { conta.setSenha(senha); administradorRepository.save(conta); },
                            () -> { throw new BadRequestException(MENSAGEM_TOKEN_INVALIDO); });
            case BARBEIRO -> barbeiroRepository.findById(recuperacao.getUsuarioId())
                    .filter(conta -> conta.getStatus() == Status.ATIVO)
                    .ifPresentOrElse(conta -> { conta.setSenha(senha); barbeiroRepository.save(conta); },
                            () -> { throw new BadRequestException(MENSAGEM_TOKEN_INVALIDO); });
            case CLIENTE -> clienteRepository.findById(recuperacao.getUsuarioId())
                    .filter(conta -> conta.getStatus() == Status.ATIVO)
                    .ifPresentOrElse(conta -> { conta.setSenha(senha); clienteRepository.save(conta); },
                            () -> { throw new BadRequestException(MENSAGEM_TOKEN_INVALIDO); });
        }
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível processar o token de recuperação.", e);
        }
    }

    private record ContaRecuperacao(Long id, String email, NivelAcesso nivelAcesso, Status status) {}
}
