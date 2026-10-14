package br.com.qrserve.application.pedido;

public record CriarPedidoCommand(
        String tokenUsuario,
        Integer menuItemId,
        Integer quantidade
) {
}
