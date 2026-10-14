package br.com.qrserve.application.pedido;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.domain.exception.MenuItemNaoEncontradoException;
import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.menu.MenuItem;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.domain.pedido.StatusPedido;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.infrastructure.persistence.menu.MenuItemRepository;
import br.com.qrserve.infrastructure.persistence.pedido.PedidoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CriarPedidoUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CriarPedidoUseCase.class);

    private final ParticipanteSessaoRepository participanteSessaoRepository;
    private final MenuItemRepository menuItemRepository;
    private final PedidoRepository pedidoRepository;
    private final TimeProvider timeProvider;

    public CriarPedidoUseCase(ParticipanteSessaoRepository participanteSessaoRepository, MenuItemRepository menuItemRepository, PedidoRepository pedidoRepository, TimeProvider timeProvider) {
        this.participanteSessaoRepository = participanteSessaoRepository;
        this.menuItemRepository = menuItemRepository;
        this.pedidoRepository = pedidoRepository;
        this.timeProvider = timeProvider;
    }

    @Transactional(rollbackOn = Exception.class)
    public CriarPedidoResult execute(CriarPedidoCommand command) {
        ParticipanteSessao participanteSessao = obterParticipanteSessao(command);
        MenuItem menuItem = obterMenuItem(command);
        validarPedidoPermitido(participanteSessao, menuItem);

        Pedido pedido = new Pedido();
        pedido.setParticipanteSessaoId(participanteSessao);
        pedido.setMenuItemId(menuItem);
        pedido.setValorUnitario(menuItem.getPreco());
        pedido.setQuantidade(command.quantidade());
        pedido.setStatus(StatusPedido.PEDIDO_REALIZADO);
        pedido.setDataHoraPedido(timeProvider.dataHoraAtual());

        return CriarPedidoResult.from(pedidoRepository.save(pedido));
    }

    private void validarPedidoPermitido(ParticipanteSessao participanteSessao, MenuItem menuItem) {
        SessaoMesa sessao = participanteSessao.getSessao();

        if (sessao.isEncerrada()) {
            LOGGER.error("Sessão encerrada -- sessaoId: {}", sessao.getId());
            throw new BusinessException("A sessão da mesa foi encerrada, não é possível realizar pedidos");
        }

        if (!Objects.equals(sessao.getEstabelecimento().getId(), menuItem.getEstabelecimento().getId())) {
            LOGGER.error("Item do menu de outro estabelecimento -- sessaoId: {} / menuItemId: {}", sessao.getId(), menuItem.getId());
            throw new MenuItemNaoEncontradoException("Não foi encontrado o item do menu para realizar o pedido, tente novamente");
        }

        if (!menuItem.isDisponivel()) {
            LOGGER.error("Item do menu indisponível -- menuItemId: {}", menuItem.getId());
            throw new BusinessException("O item do menu está indisponível no momento");
        }
    }

    private MenuItem obterMenuItem(CriarPedidoCommand command) {
        return menuItemRepository.findById(command.menuItemId())
                .orElseThrow(() -> {
                    LOGGER.error("Item do menu não encontrado -- {}", command);
                    return new MenuItemNaoEncontradoException("Não foi encontrado o item do menu para realizar o pedido, tente novamente");
                });
    }

    private ParticipanteSessao obterParticipanteSessao(CriarPedidoCommand command) {
        return participanteSessaoRepository
                .obterParticipantePorToken(command.tokenUsuario())
                .orElseThrow(() -> {
                    LOGGER.error("Usuário não encontrado -- {}", command);
                    return new ParticipanteSessaoNaoEncontradoException("Usuário não reconhecido, por favor tente novamente");
                });
    }

}
