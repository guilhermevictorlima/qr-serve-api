package br.com.qrserve.presentation.mesa.response;

import br.com.qrserve.domain.mesa.Mesa;

public record MesaResponse(
    Integer id,
    Integer numeroMesa
) {
    public static MesaResponse from(Mesa mesa) {
        return new MesaResponse(mesa.getId(), mesa.getNumeroMesa());
    }
}
