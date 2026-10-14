package br.com.qrserve.application;

import br.com.qrserve.application.pedido.CriarPedidoCommand;
import br.com.qrserve.application.sessao.RespostaSolicitacao;
import br.com.qrserve.application.sessao.ResponderSolicitacaoEntradaSessaoCommand;
import br.com.qrserve.presentation.pedido.form.CriarPedidoForm;
import br.com.qrserve.presentation.sessao.form.RespostaSolicitacaoEntradaSessao;
import br.com.qrserve.presentation.sessao.form.ResponderSolicitacaoEntradaSessaoForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class LogSemTokenTest {

    private static final String TOKEN_SECRETO = "token-secreto";
    private static final String OUTRO_TOKEN_SECRETO = "outro-token-secreto";

    private String texto;

    @Test
    @DisplayName("Dado command de criar pedido, quando converter para texto, não deve expor o token")
    void dadoCommandDeCriarPedidoQuandoConverterParaTextoNaoDeveExporToken() {
        texto = new CriarPedidoCommand(TOKEN_SECRETO, 1, 2).toString();
        naoDeveExporTokens();
    }

    @Test
    @DisplayName("Dado form de criar pedido, quando converter para texto, não deve expor o token")
    void dadoFormDeCriarPedidoQuandoConverterParaTextoNaoDeveExporToken() {
        texto = new CriarPedidoForm(TOKEN_SECRETO, 1, 2).toString();
        naoDeveExporTokens();
    }

    @Test
    @DisplayName("Dado command de responder solicitação, quando converter para texto, não deve expor os tokens")
    void dadoCommandDeResponderSolicitacaoQuandoConverterParaTextoNaoDeveExporTokens() {
        texto = new ResponderSolicitacaoEntradaSessaoCommand(TOKEN_SECRETO, OUTRO_TOKEN_SECRETO, RespostaSolicitacao.APROVADA).toString();
        naoDeveExporTokens();
    }

    @Test
    @DisplayName("Dado form de responder solicitação, quando converter para texto, não deve expor os tokens")
    void dadoFormDeResponderSolicitacaoQuandoConverterParaTextoNaoDeveExporTokens() {
        texto = new ResponderSolicitacaoEntradaSessaoForm(TOKEN_SECRETO, OUTRO_TOKEN_SECRETO, RespostaSolicitacaoEntradaSessao.SOLICITACAO_APROVADA).toString();
        naoDeveExporTokens();
    }

    private void naoDeveExporTokens() {
        assertFalse(texto.contains(TOKEN_SECRETO));
        assertFalse(texto.contains(OUTRO_TOKEN_SECRETO));
    }
}
