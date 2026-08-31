package br.com.qrserve.domain.sessao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TokenUsuario(
    @Column(name = "token", nullable = false, unique = true)
    String valor
) {
    public TokenUsuario {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Token não pode ser vazio");
        }
    }

    public static TokenUsuario create() {
        return new TokenUsuario(UUID.randomUUID().toString());
    }

}
