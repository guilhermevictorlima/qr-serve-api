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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PedidoServiceTest {

    @Mock
    private ParticipanteSessaoRepository participanteSessaoRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private TimeConfig timeConfig;

    private PedidoService pedidoService;

    private CriarPedidoForm form;
    private ParticipanteSessao participanteSessao;
    private MenuItem menuItem;
    private CriarPedidoResponse resultado;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(participanteSessaoRepository, menuItemRepository, pedidoRepository, timeConfig);

        when(timeConfig.dataHoraAtual()).thenReturn(LocalDateTime.of(2026, 8, 31, 11, 40, 50));
    }

    private void dadoFormulario() {
        form = new CriarPedidoForm("token-abc-123", 10, new BigDecimal("25.90"), 2);
    }

    private void dadoParticipanteSessaoExistente() {
        participanteSessao = new ParticipanteSessao();
        participanteSessao.setId(1);
        participanteSessao.setNome("João da Silva");

        when(participanteSessaoRepository.obterParticipantePorToken(form.tokenUsuario()))
                .thenReturn(Optional.of(participanteSessao));
    }

    private void dadoMenuItemExistente() {
        menuItem = new MenuItem();
        menuItem.setDescricao("X-Salada");

        when(menuItemRepository.findById(form.menuItemId()))
                .thenReturn(Optional.of(menuItem));
    }

    private void quandoCriarPedido() {
        resultado = pedidoService.criarPedido(form);
    }

    private void devePersistir() {
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());

        Pedido pedidoPersistido = captor.getValue();

        assertThat(pedidoPersistido.getParticipanteSessao().getId()).isEqualTo(participanteSessao.getId());
        assertThat(pedidoPersistido.getMenuItem().getId()).isEqualTo(menuItem.getId());
        assertThat(pedidoPersistido.getValorUnitario()).isEqualByComparingTo(form.valorUnitario());
        assertThat(pedidoPersistido.getQuantidade()).isEqualTo(form.quantidade());
        assertThat(pedidoPersistido.getStatus()).isEqualTo(StatusPedido.PEDIDO_REALIZADO);
        assertThat(pedidoPersistido.getDataHoraPedido()).isNotNull();
        assertThat(pedidoPersistido.getDataHoraCancelamento()).isNull();

        assertThat(resultado.nomeParticipanteSessao()).isEqualTo(participanteSessao.getNome());
        assertThat(resultado.descricaoMenuItem()).isEqualTo(menuItem.getDescricao());
        assertThat(resultado.valorUnitario()).isEqualByComparingTo(form.valorUnitario());
        assertThat(resultado.quantidade()).isEqualTo(form.quantidade());
    }

    private void deveLancarException(String mensagemEsperada) {
        assertThatThrownBy(this::quandoCriarPedido)
                .isInstanceOf(BusinessException.class)
                .hasMessage(mensagemEsperada);
    }

    @Test
    void criarPedidoComFormValidoDeveFuncionar() {
        dadoFormulario();
        dadoParticipanteSessaoExistente();
        dadoMenuItemExistente();

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        quandoCriarPedido();

        devePersistir();
    }

    @Test
    void criarPedidoComParticipanteSessaoInexistenteDeveLancarException() {
        dadoFormulario();
        dadoMenuItemExistente();

        deveLancarException("Usuário não encontrado");
    }

    @Test
    void criarPedidoComMenuItemInexistenteDeveLancarException() {
        dadoFormulario();
        dadoParticipanteSessaoExistente();

        deveLancarException("Item do menu não encontrado");
    }
}