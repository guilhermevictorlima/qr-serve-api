package br.com.qrserve.application.sessao;

public record ResponderSolicitacaoEntradaSessaoCommand(
        String tokenUsuarioSolicitante,
        String tokenUsuarioRespondente,
        RespostaSolicitacao resposta
) {
}
