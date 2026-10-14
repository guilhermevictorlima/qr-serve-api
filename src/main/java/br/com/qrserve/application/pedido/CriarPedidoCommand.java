package br.com.qrserve.application.pedido;

public record CriarPedidoCommand(
        String tokenUsuario,
        Integer menuItemId,
        Integer quantidade
) {

    @Override
    public String toString() {
        return "CriarPedidoCommand[menuItemId=" + menuItemId + ", quantidade=" + quantidade + "]";
    }
}
