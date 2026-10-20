package br.com.qrserve.presentation.sessao.response;

import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;

import java.time.LocalDateTime;

public record SolicitacaoEntradaSessaoResponse(
        String tokenUsuario,
        String nome,
        LocalDateTime dataHoraSolicitacao
) {
    public static SolicitacaoEntradaSessaoResponse from(SolicitacaoEntradaSessao solicitacao) {
        return new SolicitacaoEntradaSessaoResponse(solicitacao.getToken().getValor(), solicitacao.getNome(), solicitacao.getDataHoraSolicitacao());
    }
}
