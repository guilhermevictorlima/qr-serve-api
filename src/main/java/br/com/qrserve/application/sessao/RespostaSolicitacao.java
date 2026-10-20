package br.com.qrserve.application.sessao;

public enum RespostaSolicitacao {
    APROVADA,
    NEGADA;

    public boolean isAprovada() {
        return this == APROVADA;
    }
}
