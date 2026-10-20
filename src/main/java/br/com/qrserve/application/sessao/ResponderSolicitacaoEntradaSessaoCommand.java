package br.com.qrserve.application.sessao;

public record ResponderSolicitacaoEntradaSessaoCommand(
        String tokenUsuarioSolicitante,
        String tokenUsuarioRespondente,
        RespostaSolicitacao resposta
) {

    @Override
    public String toString() {
        return "ResponderSolicitacaoEntradaSessaoCommand[resposta=" + resposta + "]";
    }
}
