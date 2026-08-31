package br.com.qrserve.models.dto.response;

import br.com.qrserve.models.data.Pedido;

import java.math.BigDecimal;

public record CriarPedidoResponse(
        String nomeParticipanteSessao,
        String descricaoMenuItem,
        BigDecimal valorUnitario,
        Integer quantidade
) {

    public static CriarPedidoResponse from(Pedido pedido) {
        return new CriarPedidoResponse(
                pedido.getParticipanteSessao().getNome(),
                pedido.getMenuItem().getDescricao(),
                pedido.getValorUnitario(),
                pedido.getQuantidade()
        );
    }

}