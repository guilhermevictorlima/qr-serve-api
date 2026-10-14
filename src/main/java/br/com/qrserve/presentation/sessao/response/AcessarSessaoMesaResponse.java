package br.com.qrserve.presentation.sessao.response;

import br.com.qrserve.application.sessao.AcessarSessaoResult;

public record AcessarSessaoMesaResponse(
    String tokenUsuario,
    Integer sessaoMesaId,
    AcessarSessaoMesaResponseStatus responseStatus
) {

    public static AcessarSessaoMesaResponse from(AcessarSessaoResult result) {
        return new AcessarSessaoMesaResponse(
                result.tokenUsuario(),
                result.sessaoMesaId(),
                AcessarSessaoMesaResponseStatus.valueOf(result.status().name())
        );
    }
}
