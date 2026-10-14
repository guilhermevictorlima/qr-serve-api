package br.com.qrserve.application.pedido;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.EstadoInvalidoException;
import br.com.qrserve.domain.exception.PedidoNaoEncontradoException;
import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.domain.pedido.StatusPedido;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.infrastructure.persistence.pedido.PedidoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CancelarPedidoUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelarPedidoUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final ParticipanteSessaoRepository participanteSessaoRepository;
    private final TimeProvider timeProvider;

    public CancelarPedidoUseCase(PedidoRepository pedidoRepository, ParticipanteSessaoRepository participanteSessaoRepository, TimeProvider timeProvider) {
        this.pedidoRepository = pedidoRepository;
        this.participanteSessaoRepository = participanteSessaoRepository;
        this.timeProvider = timeProvider;
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(Integer pedidoId, String tokenUsuario) {
        ParticipanteSessao participante = obterParticipante(tokenUsuario);
        Pedido pedido = obterPedido(pedidoId);
        validarPermissao(pedido, participante);

        if (!pedido.isPedidoEmAberto()) {
            throw new EstadoInvalidoException("O pedido precisa estar em aberto para ser cancelado");
        }

        pedido.setStatus(StatusPedido.PEDIDO_CANCELADO);
        pedido.setDataHoraCancelamento(timeProvider.dataHoraAtual());

        pedidoRepository.save(pedido);
    }

    private ParticipanteSessao obterParticipante(String tokenUsuario) {
        return participanteSessaoRepository.obterParticipantePorToken(tokenUsuario)
                .orElseThrow(() -> {
                    LOGGER.error("PARTICIPANTE NÃO ENCONTRADO");
                    return new ParticipanteSessaoNaoEncontradoException("Usuário não reconhecido, por favor tente novamente");
                });
    }

    private void validarPermissao(Pedido pedido, ParticipanteSessao participante) {
        if (!pedido.isDoParticipante(participante)) {
            LOGGER.error("PARTICIPANTE SEM PERMISSÃO PARA O PEDIDO -- pedidoId: {}", pedido.getId());
            throw new PedidoNaoEncontradoException("Pedido não encontrado");
        }
    }

    private Pedido obterPedido(Integer pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> {
                    LOGGER.error("PEDIDO NÃO ENCONTRADO -- {}", pedidoId);
                    return new PedidoNaoEncontradoException("Pedido não encontrado");
                });
    }

}
