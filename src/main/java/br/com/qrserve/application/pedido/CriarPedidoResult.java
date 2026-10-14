package br.com.qrserve.application.pedido;

import br.com.qrserve.domain.pedido.Pedido;

import java.math.BigDecimal;

public record CriarPedidoResult(
        Integer id,
        String nomeParticipanteSessao,
        String nomeMenuItem,
        BigDecimal valorUnitario,
        Integer quantidade
) {

    public static CriarPedidoResult from(Pedido pedido) {
        return new CriarPedidoResult(
                pedido.getId(),
                pedido.getParticipanteSessao().getNome(),
                pedido.getMenuItem().getNome(),
                pedido.getValorUnitario(),
                pedido.getQuantidade()
        );
    }

}
