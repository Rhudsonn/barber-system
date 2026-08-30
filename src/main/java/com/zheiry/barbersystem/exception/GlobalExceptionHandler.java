package com.zheiry.barbersystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


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
