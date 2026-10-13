package br.com.qrserve.application.pedido;

import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.domain.exception.PedidoNaoEncontradoException;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.domain.pedido.StatusPedido;
import br.com.qrserve.infrastructure.persistence.pedido.PedidoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConcluirPedidoUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcluirPedidoUseCase.class);

    private final PedidoRepository pedidoRepository;

    public ConcluirPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }


    @Transactional(rollbackOn = Exception.class)
    public void execute(Integer pedidoId) {
        Pedido pedido = obterPedido(pedidoId);

        if (!pedido.isPedidoEmAberto()) {
            throw new BusinessException("O pedido precisa estar em aberto para ser concluído");
        }

        pedido.setStatus(StatusPedido.PEDIDO_CONCLUIDO);

        pedidoRepository.save(pedido);
    }

    private Pedido obterPedido(Integer pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> {
                    LOGGER.error("PEDIDO NÃO ENCONTRADO -- {}", pedidoId);
                    return new PedidoNaoEncontradoException("Pedido não encontrado");
                });
    }

}
