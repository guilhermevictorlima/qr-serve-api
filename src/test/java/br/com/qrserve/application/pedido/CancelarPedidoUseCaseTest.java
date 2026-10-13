package br.com.qrserve.application.pedido;

import br.com.qrserve.domain.exception.PedidoNaoEncontradoException;
import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.domain.pedido.StatusPedido;
import br.com.qrserve.infrastructure.persistence.pedido.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarPedidoUseCaseTest {

    private static final LocalDateTime DATA_HORA_ATUAL =
            LocalDateTime.of(2026, 9, 1, 12, 34, 50);

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private TimeProvider timeProvider;

    @Captor
    private ArgumentCaptor<Pedido> pedidoArgumentCaptor;

    @InjectMocks
    private CancelarPedidoUseCase useCase;

    private Pedido pedido;
    private Exception exception;
    private Class<? extends Exception> tipoEsperadoException;
    private String mensagemEsperadaException;

    @Test
    @DisplayName("Quando o pedido existir e estiver em aberto, cancelar o pedido")
    void cenario_1() {
        dadoPedidoEmAberto();
        quandoCancelarPedido();
        deveCancelarPedido();
    }

    @Test
    @DisplayName("Quando o pedido não existir, rejeitar o cancelamento")
    void cenario_2() {
        dadoPedidoInexistente();
        quandoCancelarPedido();
        deveRejeitarCancelamentoDoPedido();
    }

    @Test
    @DisplayName("Quando o pedido não estiver em aberto, rejeitar o cancelamento")
    void cenario_3() {
        dadoPedidoNaoEstiverEmAberto();
        quandoCancelarPedido();
        deveRejeitarCancelamentoDoPedido();
    }

    private void dadoPedidoEmAberto() {
        pedido = new Pedido();
        pedido.setStatus(StatusPedido.PEDIDO_REALIZADO);

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));

        when(timeProvider.dataHoraAtual())
                .thenReturn(DATA_HORA_ATUAL);
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

        mensagemEsperadaException =
                "O pedido precisa estar em aberto para ser cancelado";
        tipoEsperadoException = BusinessException.class;
    }

    private void quandoCancelarPedido() {
        try {
            useCase.execute(1);
        } catch (Exception e) {
            exception = e;
        }
    }

    private void deveCancelarPedido() {
        verify(pedidoRepository)
                .save(pedidoArgumentCaptor.capture());

        Pedido pedidoSalvo = pedidoArgumentCaptor.getValue();

        assertEquals(StatusPedido.PEDIDO_CANCELADO, pedidoSalvo.getStatus());
        assertEquals(DATA_HORA_ATUAL, pedidoSalvo.getDataHoraCancelamento());
    }

    private void deveRejeitarCancelamentoDoPedido() {
        assertNotNull(exception);
        assertInstanceOf(tipoEsperadoException, exception);
        assertEquals(mensagemEsperadaException, exception.getMessage());

        verify(pedidoRepository, never())
                .save(any(Pedido.class));
    }
}