package br.com.qrserve.presentation.pedido;

import br.com.qrserve.presentation.pedido.form.CriarPedidoForm;
import br.com.qrserve.presentation.pedido.response.CriarPedidoResponse;
import br.com.qrserve.application.pedido.PedidoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedido")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public CriarPedidoResponse realizarPedido(@Valid @RequestBody CriarPedidoForm form) {
        return pedidoService.criarPedido(form);
    }

    @PatchMapping("/{id}/cancelar")
    public void cancelarPedido(@PathVariable Integer id) {
        pedidoService.cancelarPedido(id);
    }

    @PatchMapping("/{id}/concluir")
    public void concluirPedido(@PathVariable Integer id) {
        pedidoService.concluirPedido(id);
    }
}