package br.com.qrserve.domain.sessao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class TokenUsuario {

    @Column(name = "token", nullable = false, unique = true)
    private String valor;

    private TokenUsuario(String valor) {
        this.valor = valor;
    }

    public static TokenUsuario create() {
        return new TokenUsuario(UUID.randomUUID().toString());
    }

    public String getValor() {
        return valor;
    }
}