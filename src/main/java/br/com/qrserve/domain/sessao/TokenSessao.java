package br.com.qrserve.domain.sessao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Embeddable
public class TokenSessao {
    private static final int TAMANHO_TOKEN = 10;

    @Column(name = "token", nullable = false, unique = true)
    private String valor;

    public TokenSessao() {}

    private TokenSessao(String valor) {
        this.valor = valor;
    }

    public static TokenSessao create(Integer mesaId, LocalDateTime dataHoraAtual) {
        String baseToken = RandomStringUtils.randomAlphanumeric(TAMANHO_TOKEN);
        return new TokenSessao(baseToken + mesaId + dataHoraAtual.format(DateTimeFormatter.ofPattern("ddMMyy")));
    }

    public String getValor() {
        return valor;
    }
}
