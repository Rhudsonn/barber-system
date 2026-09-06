package com.zheiry.barbersystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {

    // Email ou senha errados no /login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> credenciaisInvalidas(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos");
    }

    // Usuário autenticado, mas sem a role exigida (@PreAuthorize barrou)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> acessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para esta ação");
    }

    // Erros de @Valid nos DTOs (campo obrigatório, formato inválido, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacaoInvalida(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> erros.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }

    // Recurso não encontrado 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> recursoNaoEncontrado(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Recurso duplicado 400/409
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<String> duplicado(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }


    // Erros causados por dados inválidos enviados pelo cliente - 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> invalidacao(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }



}
