package br.com.qrserve.presentation.pedido.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarPedidoForm(

        @NotBlank(message = "O token de usuário é obrigatório para criar pedido")
        String tokenUsuario,

        @NotNull(message = "O identificador do item de menu é obrigatório para criar pedido")
        Integer menuItemId,

        @NotNull(message = "A quantidade de itens é obrigatória para criar pedido")
        @Min(value = 1, message = "A quantidade de itens deve ser maior que zero para criar pedido")
        Integer quantidade
) {
}