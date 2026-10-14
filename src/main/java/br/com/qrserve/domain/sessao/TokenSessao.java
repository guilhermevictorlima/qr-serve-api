package br.com.qrserve.domain.sessao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Embeddable
public class TokenSessao {
    private static final int TAMANHO_TOKEN = 24;
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Column(name = "token", nullable = false, unique = true)
    private String valor;

    public TokenSessao() {}

    private TokenSessao(String valor) {
        this.valor = valor;
    }

    public static TokenSessao create(Integer mesaId, LocalDateTime dataHoraAtual) {
        String baseToken = gerarBaseToken();
        return new TokenSessao(baseToken + mesaId + dataHoraAtual.format(DateTimeFormatter.ofPattern("ddMMyy")));
    }

    private static String gerarBaseToken() {
        StringBuilder base = new StringBuilder(TAMANHO_TOKEN);
        for (int i = 0; i < TAMANHO_TOKEN; i++) {
            base.append(CARACTERES.charAt(SECURE_RANDOM.nextInt(CARACTERES.length())));
        }
        return base.toString();
    }

    public String getValor() {
        return valor;
    }
}
