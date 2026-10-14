package br.com.qrserve.presentation.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private BeanPropertyBindingResult bindingResult;
    private ResponseEntity<Object> response;

    @Test
    @DisplayName("Dado campo com duas violações, quando tratar erro de validação, deve juntar as mensagens")
    void dadoCampoComDuasViolacoesQuandoTratarErroDeValidacaoDeveJuntarAsMensagens() throws Exception {
        dadoResultadoDeValidacao();
        dadoErroNoCampo("nome", "não pode ser vazio");
        dadoErroNoCampo("nome", "tamanho inválido");
        quandoTratarErroDeValidacao();
        deveRetornarMensagensDoCampo("nome", "não pode ser vazio; tamanho inválido");
    }

    @Test
    @DisplayName("Dado violação sem mensagem, quando tratar erro de validação, deve usar mensagem padrão")
    void dadoViolacaoSemMensagemQuandoTratarErroDeValidacaoDeveUsarMensagemPadrao() throws Exception {
        dadoResultadoDeValidacao();
        dadoErroNoCampo("nome", null);
        quandoTratarErroDeValidacao();
        deveRetornarMensagensDoCampo("nome", "Valor inválido");
    }

    private void dadoResultadoDeValidacao() {
        bindingResult = new BeanPropertyBindingResult(new Object(), "form");
    }

    private void dadoErroNoCampo(String campo, String mensagem) {
        bindingResult.addError(new FieldError("form", campo, null, false, null, null, mensagem));
    }

    private void quandoTratarErroDeValidacao() throws Exception {
        var metodo = Object.class.getMethod("equals", Object.class);
        var excecao = new MethodArgumentNotValidException(new MethodParameter(metodo, 0), bindingResult);

        response = handler.handleMethodArgumentNotValid(
                excecao,
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                new ServletWebRequest(new MockHttpServletRequest())
        );
    }

    @SuppressWarnings("unchecked")
    private void deveRetornarMensagensDoCampo(String campo, String mensagemEsperada) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(mensagemEsperada, ((Map<String, String>) response.getBody()).get(campo));
    }
}
