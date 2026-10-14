package br.com.qrserve.presentation.sessao.form;

import br.com.qrserve.application.sessao.ResponderSolicitacaoEntradaSessaoCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record ResponderSolicitacaoEntradaSessaoForm(
    @NotBlank(message = "O token do usuário solicitante é obrigatório para responder a solicitação") String tokenUsuarioSolicitante,
    @NotBlank(message = "O token do usuário respondente é obrigatório para responder a solicitação") String tokenUsuarioRespondente,
    @NotNull(message = "A resposta da solicitação é obrigatória para responder a solicitação") RespostaSolicitacaoEntradaSessao resposta
) {

    public ResponderSolicitacaoEntradaSessaoCommand toCommand() {
        return new ResponderSolicitacaoEntradaSessaoCommand(tokenUsuarioSolicitante, tokenUsuarioRespondente, resposta.toRespostaSolicitacao());
    }
}
