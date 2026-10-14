package br.com.qrserve.presentation.pedido.response;

import br.com.qrserve.application.pedido.CriarPedidoResult;

import java.math.BigDecimal;

public record CriarPedidoResponse(
        Integer id,
        String nomeParticipanteSessao,
        String descricaoMenuItem,
        BigDecimal valorUnitario,
        Integer quantidade
) {

    public static CriarPedidoResponse from(CriarPedidoResult result) {
        return new CriarPedidoResponse(
                result.id(),
                result.nomeParticipanteSessao(),
                result.descricaoMenuItem(),
                result.valorUnitario(),
                result.quantidade()
        );
    }

}
