package br.com.qrserve.presentation.exception;

import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.domain.exception.EstadoInvalidoException;
import br.com.qrserve.domain.exception.RecursoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String MENSAGEM_ERRO_INESPERADO = "Ocorreu um erro inesperado, tente novamente mais tarde";
    private static final String MENSAGEM_CAMPO_INVALIDO = "Valor inválido";
    private static final String MENSAGEM_REQUISICAO_INVALIDA = "Requisição inválida";
    private static final String MENSAGEM_CONFLITO_CONCORRENCIA = "O recurso foi alterado por outra operação, tente novamente";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErroResponse> handle(
            BusinessException exception
    ) {
        return ResponseEntity.badRequest().body(ErroResponse.of(exception.getMessage()));
    }

    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<ErroResponse> handle(
            EstadoInvalidoException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.of(exception.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handle(
            RecursoNaoEncontradoException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse.of(exception.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> handle(
            ObjectOptimisticLockingFailureException exception
    ) {
        LOGGER.warn("CONFLITO DE ATUALIZAÇÃO CONCORRENTE", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.of(MENSAGEM_CONFLITO_CONCORRENCIA));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handle(
            Exception exception
    ) {
        LOGGER.error("ERRO INESPERADO", exception);
        return ResponseEntity.internalServerError().body(ErroResponse.of(MENSAGEM_ERRO_INESPERADO));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> erros = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        erro -> Objects.toString(erro.getDefaultMessage(), MENSAGEM_CAMPO_INVALIDO),
                        (primeira, segunda) -> primeira + "; " + segunda
                ));

        return ResponseEntity.badRequest().body(ErroResponse.of(MENSAGEM_REQUISICAO_INVALIDA, erros));
    }

}
