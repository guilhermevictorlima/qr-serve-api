package br.com.qrserve.models.dto.response;

import br.com.qrserve.models.data.Mesa;

public record MesaResponse(
    Integer id,
    Integer numeroMesa
) {
    public static MesaResponse from(Mesa mesa) {
        return new MesaResponse(mesa.getId(), mesa.getNumeroMesa());
    }
}
