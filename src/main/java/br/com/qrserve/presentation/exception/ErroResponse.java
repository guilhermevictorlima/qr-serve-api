package br.com.qrserve.presentation.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
    String mensagem,
    Map<String, String> campos
) {
    public static ErroResponse of(String mensagem) {
        return new ErroResponse(mensagem, null);
    }

    public static ErroResponse of(String mensagem, Map<String, String> campos) {
        return new ErroResponse(mensagem, campos);
    }
}
