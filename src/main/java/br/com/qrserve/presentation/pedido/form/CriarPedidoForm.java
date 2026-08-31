package br.com.qrserve.presentation.pedido.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CriarPedidoForm(

        @NotBlank(message = "O token de usuário é obrigatório para criar pedido")
        String tokenUsuario,

        @NotNull(message = "O identificador do item de menu é obrigatório para criar pedido")
        Integer menuItemId,

        @NotNull(message = "O valor unitário é obrigatório para criar pedido")
        @DecimalMin(value = "0.0", inclusive = false, message = "O valor unitário deve ser maior que zero para criar pedido")
        BigDecimal valorUnitario,

        @NotNull(message = "A quantidade de itens é obrigatória para criar pedido")
        @Min(value = 1, message = "A quantidade de itens deve ser maior que zero para criar pedido")
        Integer quantidade
) {
}