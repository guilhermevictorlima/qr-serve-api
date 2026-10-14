package br.com.qrserve.application.pedido;

import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.exception.MenuItemNaoEncontradoException;
import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.menu.MenuItem;
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

    private void dadoParticipanteSessao() {
        participanteSessao = new ParticipanteSessao(
                new SessaoMesa(1),
                TokenUsuario.create(),
                "Guilherme"
        );

        tokenUsuario = participanteSessao.getToken().getValor();

        when(participanteSessaoRepository
                .obterParticipantePorToken(tokenUsuario))
                .thenReturn(Optional.of(participanteSessao));
    }

    private void dadoMenuItem() {
        menuItem = new MenuItem();
        menuItem.setPreco(PRECO_MENU_ITEM);

        when(menuItemRepository.findById(MENU_ITEM_ID))
                .thenReturn(Optional.of(menuItem));
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