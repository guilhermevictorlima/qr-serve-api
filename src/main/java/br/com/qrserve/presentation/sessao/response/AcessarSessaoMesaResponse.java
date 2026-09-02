package br.com.qrserve.presentation.sessao.response;

public record AcessarSessaoMesaResponse(
    String tokenUsuario,
    Integer sessaoMesaId,
    AcessarSessaoMesaResponseStatus responseStatus
) {}
