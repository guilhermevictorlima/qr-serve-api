package br.com.qrserve.presentation.pedido.response;

import br.com.qrserve.domain.pedido.Pedido;

import java.math.BigDecimal;

public record CriarPedidoResponse(
        Integer id,
        String nomeParticipanteSessao,
        String descricaoMenuItem,
        BigDecimal valorUnitario,
        Integer quantidade
) {

    public static CriarPedidoResponse from(Pedido pedido) {
        return new CriarPedidoResponse(
                pedido.getId(),
                pedido.getParticipanteSessao().getNome(),
                pedido.getMenuItem().getDescricao(),
                pedido.getValorUnitario(),
                pedido.getQuantidade()
        );
    }

}