package br.com.qrserve.presentation.pedido;

import br.com.qrserve.application.pedido.CancelarPedidoUseCase;
import br.com.qrserve.application.pedido.ConcluirPedidoUseCase;
import br.com.qrserve.application.pedido.CriarPedidoUseCase;
import br.com.qrserve.presentation.pedido.form.CriarPedidoForm;
import br.com.qrserve.presentation.pedido.response.CriarPedidoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedido")
public class PedidoController {
    private static final String TOKEN_USUARIO_HEADER = "Token-Usuario";

    private final CriarPedidoUseCase criarPedidoUseCase;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;
    private final ConcluirPedidoUseCase concluirPedidoUseCase;

    public PedidoController(CriarPedidoUseCase criarPedidoUseCase, CancelarPedidoUseCase cancelarPedidoUseCase, ConcluirPedidoUseCase concluirPedidoUseCase) {
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.cancelarPedidoUseCase = cancelarPedidoUseCase;
        this.concluirPedidoUseCase = concluirPedidoUseCase;
    }

    @PostMapping
    public CriarPedidoResponse realizarPedido(@Valid @RequestBody CriarPedidoForm form) {
        return criarPedidoUseCase.execute(form);
    }

    @PatchMapping("/{id}/cancelar")
    public void cancelarPedido(@PathVariable Integer id, @RequestHeader(TOKEN_USUARIO_HEADER) String tokenUsuario) {
        cancelarPedidoUseCase.execute(id, tokenUsuario);
    }

    @PatchMapping("/{id}/concluir")
    public void concluirPedido(@PathVariable Integer id, @RequestHeader(TOKEN_USUARIO_HEADER) String tokenUsuario) {
        concluirPedidoUseCase.execute(id, tokenUsuario);
    }
}