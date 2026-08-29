package br.com.qrserve.models.dto.form;

public enum RespostaSolicitacaoEntradaSessao {
    SOLICITACAO_APROVADA,
    SOLICITACAO_NEGADA;

    public boolean isSolicitacaoAprovada() {
        return this == SOLICITACAO_APROVADA;
    }
}
