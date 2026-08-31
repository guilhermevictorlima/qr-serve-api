package br.com.qrserve.services;

import br.com.qrserve.config.TimeConfig;
import br.com.qrserve.exceptions.BusinessException;
import br.com.qrserve.models.data.cardapio.MenuItem;
import br.com.qrserve.models.data.ParticipanteSessao;
import br.com.qrserve.models.data.Pedido;
import br.com.qrserve.models.data.StatusPedido;
import br.com.qrserve.models.dto.form.CriarPedidoForm;
import br.com.qrserve.models.dto.response.CriarPedidoResponse;
import br.com.qrserve.repositories.MenuItemRepository;
import br.com.qrserve.repositories.ParticipanteSessaoRepository;
import br.com.qrserve.repositories.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PedidoService {

    private final ParticipanteSessaoRepository participanteSessaoRepository;
    private final MenuItemRepository menuItemRepository;
    private final PedidoRepository pedidoRepository;
    private final TimeConfig timeConfig;

    public PedidoService(ParticipanteSessaoRepository participanteSessaoRepository,
                         MenuItemRepository menuItemRepository,
                         PedidoRepository pedidoRepository, TimeConfig timeConfig) {
        this.participanteSessaoRepository = participanteSessaoRepository;
        this.menuItemRepository = menuItemRepository;
        this.pedidoRepository = pedidoRepository;
        this.timeConfig = timeConfig;
    }

    public CriarPedidoResponse criarPedido(CriarPedidoForm form) {
        ParticipanteSessao participanteSessao = participanteSessaoRepository
                .obterParticipantePorToken(form.tokenUsuario())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        MenuItem menuItem = menuItemRepository.findById(form.menuItemId())
                .orElseThrow(() -> new BusinessException("Item do menu não encontrado"));

        if (form.quantidade() == null || form.quantidade() <= 0) {
            throw new BusinessException("A quantidade de itens deve ser maior que zero");
        }

        if (form.valorUnitario() == null || form.valorUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor unitário deve ser maior que zero");
        }

        Pedido pedido = new Pedido();
        pedido.setParticipanteSessaoId(participanteSessao);
        pedido.setMenuItemId(menuItem);
        pedido.setValorUnitario(form.valorUnitario());
        pedido.setQuantidade(form.quantidade());
        pedido.setStatus(StatusPedido.PEDIDO_REALIZADO);
        pedido.setDataHoraPedido(timeConfig.dataHoraAtual());

        pedidoRepository.save(pedido);

        return CriarPedidoResponse.from(pedido);
    }

    public void cancelarPedido(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pedido não encontrado"));

        if (pedido.getStatus() != StatusPedido.PEDIDO_REALIZADO) {
            throw new BusinessException("Somente pedidos com status PEDIDO_REALIZADO podem ser cancelados");
        }

        pedido.setStatus(StatusPedido.PEDIDO_CANCELADO);
        pedido.setDataHoraCancelamento(timeConfig.dataHoraAtual());

        pedidoRepository.save(pedido);
    }

    public void concluirPedido(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pedido não encontrado"));

        if (pedido.getStatus() != StatusPedido.PEDIDO_REALIZADO) {
            throw new BusinessException("Somente pedidos com status PEDIDO_REALIZADO podem ser concluídos");
        }

        pedido.setStatus(StatusPedido.PEDIDO_PRONTO);

        pedidoRepository.save(pedido);
    }
}