package br.com.qrserve.presentation.exception;

import br.com.qrserve.domain.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handle(
            BusinessException exception
    ) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}