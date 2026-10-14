package br.com.qrserve.presentation.sessao.form;

import br.com.qrserve.application.sessao.RespostaSolicitacao;

public enum RespostaSolicitacaoEntradaSessao {
    SOLICITACAO_APROVADA(RespostaSolicitacao.APROVADA),
    SOLICITACAO_NEGADA(RespostaSolicitacao.NEGADA);

    private final RespostaSolicitacao respostaSolicitacao;

    RespostaSolicitacaoEntradaSessao(RespostaSolicitacao respostaSolicitacao) {
        this.respostaSolicitacao = respostaSolicitacao;
    }

    public RespostaSolicitacao toRespostaSolicitacao() {
        return respostaSolicitacao;
    }

}
