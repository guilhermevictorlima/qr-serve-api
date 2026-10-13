package br.com.qrserve.application.pedido;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.domain.pedido.StatusPedido;
import br.com.qrserve.infrastructure.persistence.pedido.PedidoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CancelarPedidoUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelarPedidoUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final TimeProvider timeProvider;

    public CancelarPedidoUseCase(PedidoRepository pedidoRepository, TimeProvider timeProvider) {
        this.pedidoRepository = pedidoRepository;
        this.timeProvider = timeProvider;
    }


    @Transactional(rollbackOn = Exception.class)
    public void execute(Integer pedidoId) {
        Pedido pedido = obterPedido(pedidoId);

        if (!pedido.isPedidoEmAberto()) {
            throw new BusinessException("O pedido precisa estar em aberto para ser cancelado");
        }

        pedido.setStatus(StatusPedido.PEDIDO_CANCELADO);
        pedido.setDataHoraCancelamento(timeProvider.dataHoraAtual());

        pedidoRepository.save(pedido);
    }

    private Pedido obterPedido(Integer pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> {
                    LOGGER.error("PEDIDO NÃO ENCONTRADO -- {}", pedidoId);
                   return new IllegalArgumentException("Pedido não encontrado");
                });
    }

}
