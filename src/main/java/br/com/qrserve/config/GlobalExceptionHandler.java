package br.com.qrserve.config;

import br.com.qrserve.exceptions.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> tratarErroDeValidacao(
            BusinessException exception
    ) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}