package br.com.qrserve.domain.sessao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenSessaoTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 9, 1, 12, 34, 50);

    private TokenSessao token;
    private TokenSessao outroToken;

    @Test
    @DisplayName("Dado mesa e data, quando criar token, deve ser alfanumérico e terminar com mesa e data")
    void dadoMesaEDataQuandoCriarTokenDeveSerAlfanumericoETerminarComMesaEData() {
        quandoCriarToken();
        deveSerAlfanumericoETerminarComMesaEData();
    }

    @Test
    @DisplayName("Dado mesma mesa e data, quando criar dois tokens, devem ser diferentes")
    void dadoMesmaMesaEDataQuandoCriarDoisTokensDevemSerDiferentes() {
        quandoCriarDoisTokens();
        devemSerDiferentes();
    }

    private void quandoCriarToken() {
        token = TokenSessao.create(7, DATA_HORA);
    }

    private void quandoCriarDoisTokens() {
        token = TokenSessao.create(7, DATA_HORA);
        outroToken = TokenSessao.create(7, DATA_HORA);
    }

    private void deveSerAlfanumericoETerminarComMesaEData() {
        assertTrue(token.getValor().matches("[A-Za-z0-9]{24}7010926"));
    }

    private void devemSerDiferentes() {
        assertNotEquals(token.getValor(), outroToken.getValor());
    }
}
