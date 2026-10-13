package br.com.qrserve.application.pedido;

import br.com.qrserve.domain.exception.PedidoNaoEncontradoException;
import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.domain.pedido.StatusPedido;
import br.com.qrserve.infrastructure.persistence.pedido.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcluirPedidoUseCaseTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Captor
    private ArgumentCaptor<Pedido> pedidoArgumentCaptor;

    @InjectMocks
    private ConcluirPedidoUseCase useCase;

    private Pedido pedido;
    private Exception exception;
    private Class<? extends Exception> tipoEsperadoException;
    private String mensagemEsperadaException;

    @Test
    @DisplayName("Quando o pedido existir e estiver em aberto, concluir o pedido")
    void cenario_1() {
        dadoPedidoEmAberto();
        quandoConcluirPedido();
        deveConcluirPedido();
    }

    @Test
    @DisplayName("Quando o pedido não existir, rejeitar a conclusão")
    void cenario_2() {
        dadoPedidoInexistente();
        quandoConcluirPedido();
        deveRejeitarConclusaoDoPedido();
    }

    @Test
    @DisplayName("Quando o pedido não estiver em aberto, rejeitar a conclusão")
    void cenario_3() {
        dadoPedidoNaoEstiverEmAberto();
        quandoConcluirPedido();
        deveRejeitarConclusaoDoPedido();
    }

    private void dadoPedidoEmAberto() {
        pedido = new Pedido();
        pedido.setStatus(StatusPedido.PEDIDO_REALIZADO);

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));
    }

    private void dadoPedidoInexistente() {
        when(pedidoRepository.findById(1))
                .thenReturn(Optional.empty());

        mensagemEsperadaException = "Pedido não encontrado";
        tipoEsperadoException = PedidoNaoEncontradoException.class;
    }

    private void dadoPedidoNaoEstiverEmAberto() {
        pedido = new Pedido();
        pedido.setStatus(StatusPedido.PEDIDO_CONCLUIDO);

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));

        mensagemEsperadaException = "O pedido precisa estar em aberto para ser concluído";
        tipoEsperadoException = BusinessException.class;
    }

    private void quandoConcluirPedido() {
        try {
            useCase.execute(1);
        } catch (Exception e) {
            exception = e;
        }
    }

    private void deveConcluirPedido() {
        verify(pedidoRepository)
                .save(pedidoArgumentCaptor.capture());

        Pedido pedidoSalvo = pedidoArgumentCaptor.getValue();
        assertEquals(StatusPedido.PEDIDO_CONCLUIDO, pedidoSalvo.getStatus());
    }

    private void deveRejeitarConclusaoDoPedido() {
        assertNotNull(exception);
        assertInstanceOf(tipoEsperadoException, exception);
        assertEquals(mensagemEsperadaException, exception.getMessage());

        verify(pedidoRepository, never())
                .save(any(Pedido.class));
    }

}