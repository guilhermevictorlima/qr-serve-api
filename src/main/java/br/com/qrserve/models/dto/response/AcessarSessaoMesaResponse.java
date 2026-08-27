package br.com.qrserve.models.dto.response;

public record AcessarSessaoMesaResponse(
    String tokenUsuario,
    Integer sessaoMesaId,
    AcessarSessaoMesaResponseStatus responseStatus
) {}
