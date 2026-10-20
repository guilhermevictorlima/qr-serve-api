package br.com.qrserve.domain.exception;

public abstract class RecursoNaoEncontradoException extends RuntimeException {

    protected RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
