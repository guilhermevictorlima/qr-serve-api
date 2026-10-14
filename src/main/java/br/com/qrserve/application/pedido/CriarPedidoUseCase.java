package br.com.qrserve.application.pedido;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.domain.exception.MenuItemNaoEncontradoException;
import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.menu.MenuItem;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.domain.pedido.StatusPedido;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.infrastructure.persistence.menu.MenuItemRepository;
import br.com.qrserve.infrastructure.persistence.pedido.PedidoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.presentation.pedido.form.CriarPedidoForm;
import br.com.qrserve.presentation.pedido.response.CriarPedidoResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public CriarPedidoResponse execute(CriarPedidoForm form) {
        ParticipanteSessao participanteSessao = obterParticipanteSessao(form);
        MenuItem menuItem = obterMenuItem(form);

        Pedido pedido = new Pedido();
        pedido.setParticipanteSessaoId(participanteSessao);
        pedido.setMenuItemId(menuItem);
        pedido.setValorUnitario(menuItem.getPreco());
        pedido.setQuantidade(form.quantidade());
        pedido.setStatus(StatusPedido.PEDIDO_REALIZADO);
        pedido.setDataHoraPedido(timeProvider.dataHoraAtual());

        return CriarPedidoResponse.from(pedidoRepository.save(pedido));
    }

    private MenuItem obterMenuItem(CriarPedidoForm form) {
        return menuItemRepository.findById(form.menuItemId())
                .orElseThrow(() -> {
                    LOGGER.error("Item do menu não encontrado -- {}", form);
                    return new MenuItemNaoEncontradoException("Não foi encontrado o item do menu para realizar o pedido, tente novamente");
                });
    }

    private ParticipanteSessao obterParticipanteSessao(CriarPedidoForm form) {
        return participanteSessaoRepository
                .obterParticipantePorToken(form.tokenUsuario())
                .orElseThrow(() -> {
                    LOGGER.error("Usuário não encontrado -- {}", form);
                    return new ParticipanteSessaoNaoEncontradoException("Usuário não reconhecido, por favor tente novamente");
                });
    }

}
