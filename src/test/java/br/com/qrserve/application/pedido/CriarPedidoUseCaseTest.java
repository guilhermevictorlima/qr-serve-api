package br.com.qrserve.application.pedido;

import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.exception.MenuItemNaoEncontradoException;
import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.estabelecimento.Estabelecimento;
import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.domain.menu.Menu;
import br.com.qrserve.domain.menu.MenuItem;
import br.com.qrserve.domain.menu.MenuSecao;
import br.com.qrserve.domain.menu.StatusMenuItem;
import br.com.qrserve.domain.mesa.Mesa;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.domain.pedido.StatusPedido;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.TokenUsuario;
import br.com.qrserve.infrastructure.persistence.menu.MenuItemRepository;
import br.com.qrserve.infrastructure.persistence.pedido.PedidoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.presentation.pedido.form.CriarPedidoForm;
import br.com.qrserve.presentation.pedido.response.CriarPedidoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarPedidoUseCaseTest {

    private static final LocalDateTime DATA_HORA_ATUAL = LocalDateTime.of(2026, 9, 1, 12, 34, 50);
    private static final int MENU_ITEM_ID = 1;
    private static final int ESTABELECIMENTO_ID = 1;
    private static final int OUTRO_ESTABELECIMENTO_ID = 2;
    private static final BigDecimal PRECO_MENU_ITEM = new BigDecimal("29.90");

    @Mock
    private ParticipanteSessaoRepository participanteSessaoRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private TimeProvider timeProvider;

    @Captor
    private ArgumentCaptor<Pedido> pedidoArgumentCaptor;

    @InjectMocks
    private CriarPedidoUseCase useCase;

    private String tokenUsuario;
    private CriarPedidoForm form;
    private ParticipanteSessao participanteSessao;
    private MenuItem menuItem;
    private Pedido pedido;
    private CriarPedidoResponse response;
    private Exception exception;
    private Class<? extends Exception> tipoEsperadoException;
    private String mensagemEsperadaException;

    @BeforeEach
    void setUp() {
        lenient().when(timeProvider.dataHoraAtual()).thenReturn(DATA_HORA_ATUAL);
        lenient().when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Quando o usuário existir e o item do menu existir, criar o pedido")
    void cenario_1() {
        dadoParticipanteSessao();
        dadoMenuItem();
        quandoCriarPedido();
        deveCriarPedido();
        deveRetornarPedidoCriado();
    }

    @Test
    @DisplayName("Quando o usuário não existir, rejeitar a criação do pedido")
    void cenario_2() {
        dadoParticipanteSessaoInexistente();
        quandoCriarPedido();
        deveRejeitarCriacaoDoPedido();
    }

    @Test
    @DisplayName("Quando o item do menu não existir, rejeitar a criação do pedido")
    void cenario_3() {
        dadoParticipanteSessao();
        dadoMenuItemInexistente();
        quandoCriarPedido();
        deveRejeitarCriacaoDoPedido();
    }

    @Test
    @DisplayName("Quando a sessão do usuário estiver encerrada, rejeitar a criação do pedido")
    void cenario_4() {
        dadoParticipanteSessao();
        dadoMenuItem();
        dadoSessaoEncerrada();
        quandoCriarPedido();
        deveRejeitarCriacaoDoPedido();
    }

    @Test
    @DisplayName("Quando o item do menu for de outro estabelecimento, rejeitar a criação do pedido")
    void cenario_5() {
        dadoParticipanteSessao();
        dadoMenuItemDeOutroEstabelecimento();
        quandoCriarPedido();
        deveRejeitarCriacaoDoPedido();
    }

    @Test
    @DisplayName("Quando o item do menu estiver indisponível, rejeitar a criação do pedido")
    void cenario_6() {
        dadoParticipanteSessao();
        dadoMenuItemIndisponivel();
        quandoCriarPedido();
        deveRejeitarCriacaoDoPedido();
    }

    private void dadoParticipanteSessao() {
        dadoParticipanteSessaoDoEstabelecimento(ESTABELECIMENTO_ID);
    }

    private void dadoParticipanteSessaoDoEstabelecimento(int estabelecimentoId) {
        SessaoMesa sessao = new SessaoMesa(1);
        Mesa mesa = new Mesa(1);
        mesa.setEstabelecimento(dadoEstabelecimento(estabelecimentoId));
        sessao.setMesa(mesa);

        participanteSessao = new ParticipanteSessao(
                sessao,
                TokenUsuario.create(),
                "Guilherme"
        );

        tokenUsuario = participanteSessao.getToken().getValor();

        when(participanteSessaoRepository
                .obterParticipantePorToken(tokenUsuario))
                .thenReturn(Optional.of(participanteSessao));
    }

    private void dadoSessaoEncerrada() {
        participanteSessao.getSessao().setDataHoraEncerramento(DATA_HORA_ATUAL);

        mensagemEsperadaException = "A sessão da mesa foi encerrada, não é possível realizar pedidos";
        tipoEsperadoException = BusinessException.class;
    }

    private void dadoMenuItem() {
        dadoMenuItem(ESTABELECIMENTO_ID, StatusMenuItem.DISPONIVEL);
    }

    private void dadoMenuItem(int estabelecimentoId, StatusMenuItem status) {
        Menu menu = new Menu();
        menu.setEstabelecimento(dadoEstabelecimento(estabelecimentoId));
        MenuSecao menuSecao = new MenuSecao();
        menuSecao.setMenu(menu);

        menuItem = new MenuItem();
        menuItem.setMenuSecao(menuSecao);
        menuItem.setPreco(PRECO_MENU_ITEM);
        menuItem.setStatus(status);

        when(menuItemRepository.findById(MENU_ITEM_ID))
                .thenReturn(Optional.of(menuItem));
    }

    private void dadoMenuItemDeOutroEstabelecimento() {
        dadoMenuItem(OUTRO_ESTABELECIMENTO_ID, StatusMenuItem.DISPONIVEL);

        mensagemEsperadaException = "Não foi encontrado o item do menu para realizar o pedido, tente novamente";
        tipoEsperadoException = MenuItemNaoEncontradoException.class;
    }

    private void dadoMenuItemIndisponivel() {
        dadoMenuItem(ESTABELECIMENTO_ID, StatusMenuItem.INDISPONIVEL);

        mensagemEsperadaException = "O item do menu está indisponível no momento";
        tipoEsperadoException = BusinessException.class;
    }

    private Estabelecimento dadoEstabelecimento(int id) {
        Estabelecimento estabelecimento = new Estabelecimento();
        ReflectionTestUtils.setField(estabelecimento, "id", id);
        return estabelecimento;
    }

    private void dadoParticipanteSessaoInexistente() {
        when(participanteSessaoRepository
                .obterParticipantePorToken(tokenUsuario))
                .thenReturn(Optional.empty());

        mensagemEsperadaException = "Usuário não reconhecido, por favor tente novamente";
        tipoEsperadoException = ParticipanteSessaoNaoEncontradoException.class;
    }

    private void dadoMenuItemInexistente() {
        when(menuItemRepository.findById(MENU_ITEM_ID))
                .thenReturn(Optional.empty());

        mensagemEsperadaException = "Não foi encontrado o item do menu para realizar o pedido, tente novamente";
        tipoEsperadoException = MenuItemNaoEncontradoException.class;
    }

    private void quandoCriarPedido() {
        try {
            form = new CriarPedidoForm(tokenUsuario, MENU_ITEM_ID, 2);
            response = useCase.execute(form);
        } catch (Exception e) {
            exception = e;
        }
    }

    private void deveCriarPedido() {
        assertNull(exception);

        verify(pedidoRepository)
                .save(pedidoArgumentCaptor.capture());

        pedido = pedidoArgumentCaptor.getValue();

        assertEquals(participanteSessao.getId(), pedido.getParticipanteSessao().getId());
        assertEquals(menuItem.getId(), pedido.getMenuItem().getId());
        assertEquals(PRECO_MENU_ITEM, pedido.getValorUnitario());
        assertEquals(form.quantidade(), pedido.getQuantidade());
        assertEquals(StatusPedido.PEDIDO_REALIZADO, pedido.getStatus());
        assertEquals(DATA_HORA_ATUAL, pedido.getDataHoraPedido());
    }

    private void deveRetornarPedidoCriado() {
        assertEquals(pedido.getParticipanteSessao().getNome(), response.nomeParticipanteSessao());
        assertEquals(pedido.getMenuItem().getDescricao(), response.descricaoMenuItem());
        assertEquals(pedido.getValorUnitario(), response.valorUnitario());
        assertEquals(pedido.getQuantidade(), response.quantidade());
    }

    private void deveRejeitarCriacaoDoPedido() {
        assertNotNull(exception);
        assertInstanceOf(tipoEsperadoException, exception);
        assertEquals(mensagemEsperadaException, exception.getMessage());

        verify(pedidoRepository, never())
                .save(any(Pedido.class));
    }

}