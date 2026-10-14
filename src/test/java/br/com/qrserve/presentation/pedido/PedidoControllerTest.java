package br.com.qrserve.presentation.pedido;

import br.com.qrserve.application.pedido.CancelarPedidoUseCase;
import br.com.qrserve.application.pedido.ConcluirPedidoUseCase;
import br.com.qrserve.application.pedido.CriarPedidoUseCase;
import br.com.qrserve.domain.exception.BusinessException;
import br.com.qrserve.domain.exception.MenuItemNaoEncontradoException;
import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.exception.PedidoNaoEncontradoException;
import br.com.qrserve.domain.pedido.Pedido;
import br.com.qrserve.presentation.pedido.form.CriarPedidoForm;
import br.com.qrserve.application.pedido.CriarPedidoCommand;
import br.com.qrserve.application.pedido.CriarPedidoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    private static final String MENSAGEM_ERRO_INESPERADO = "Ocorreu um erro inesperado, tente novamente mais tarde";
    private static final String TOKEN_USUARIO = "token-valido";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CriarPedidoUseCase criarPedidoUseCase;

    @MockitoBean
    private CancelarPedidoUseCase cancelarPedidoUseCase;

    @MockitoBean
    private ConcluirPedidoUseCase concluirPedidoUseCase;

    @Test
    @DisplayName("Dado formulário válido, quando realizar pedido, deve criar pedido")
    void dadoFormularioValidoQuandoRealizarPedidoDeveCriarPedido() throws Exception {
        var form = dadoFormularioValido();

        quandoRealizarPedido(form);

        verify(criarPedidoUseCase).execute(new CriarPedidoCommand("token-valido", 1, 2));
    }

    @Test
    @DisplayName("Dado token de usuário nulo, quando realizar pedido, deve retornar erro de validação")
    void dadoTokenUsuarioNuloQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido(null, 1, 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tokenUsuario")
                        .value("O token de usuário é obrigatório para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado token de usuário vazio, quando realizar pedido, deve retornar erro de validação")
    void dadoTokenUsuarioVazioQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("", 1, 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tokenUsuario")
                        .value("O token de usuário é obrigatório para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado identificador do item de menu nulo, quando realizar pedido, deve retornar erro de validação")
    void dadoMenuItemIdNuloQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", null, 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.menuItemId")
                        .value("O identificador do item de menu é obrigatório para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado quantidade nula, quando realizar pedido, deve retornar erro de validação")
    void dadoQuantidadeNulaQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", 1, null);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantidade")
                        .value("A quantidade de itens é obrigatória para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado quantidade igual a zero, quando realizar pedido, deve retornar erro de validação")
    void dadoQuantidadeIgualAZeroQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", 1, 0);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantidade")
                        .value("A quantidade de itens deve ser maior que zero para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado formulário válido, quando realizar pedido, deve retornar o pedido criado")
    void dadoFormularioValidoQuandoRealizarPedidoDeveRetornarOPedidoCriado() throws Exception {
        var form = dadoFormularioValido();
        dadoPedidoCriadoPeloUseCase();

        quandoRealizarPedido(form)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nomeParticipanteSessao").value("Maria"))
                .andExpect(jsonPath("$.nomeMenuItem").value("Pizza"))
                .andExpect(jsonPath("$.valorUnitario").value(25.90))
                .andExpect(jsonPath("$.quantidade").value(2));
    }

    @Test
    @DisplayName("Dado corpo da requisição ausente, quando realizar pedido, deve retornar requisição inválida")
    void dadoCorpoAusenteQuandoRealizarPedidoDeveRetornarRequisicaoInvalida() throws Exception {
        quandoRealizarPedidoSemCorpo()
                .andExpect(status().isBadRequest());

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado usuário não reconhecido, quando realizar pedido, deve retornar não encontrado")
    void dadoUsuarioNaoReconhecidoQuandoRealizarPedidoDeveRetornarNaoEncontrado() throws Exception {
        var form = dadoFormularioValido();
        dadoUseCaseDeCriarLancando(new ParticipanteSessaoNaoEncontradoException("Usuário não reconhecido, por favor tente novamente"));

        quandoRealizarPedido(form)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuário não reconhecido, por favor tente novamente"));
    }

    @Test
    @DisplayName("Dado item de menu inexistente, quando realizar pedido, deve retornar não encontrado")
    void dadoItemDeMenuInexistenteQuandoRealizarPedidoDeveRetornarNaoEncontrado() throws Exception {
        var form = dadoFormularioValido();
        dadoUseCaseDeCriarLancando(new MenuItemNaoEncontradoException("Não foi encontrado o item do menu para realizar o pedido, tente novamente"));

        quandoRealizarPedido(form)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Não foi encontrado o item do menu para realizar o pedido, tente novamente"));
    }

    @Test
    @DisplayName("Dado erro inesperado, quando realizar pedido, deve retornar erro interno")
    void dadoErroInesperadoQuandoRealizarPedidoDeveRetornarErroInterno() throws Exception {
        var form = dadoFormularioValido();
        dadoUseCaseDeCriarLancando(new IllegalStateException("falha de banco"));

        quandoRealizarPedido(form)
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(MENSAGEM_ERRO_INESPERADO));
    }

    @Test
    @DisplayName("Dado pedido em aberto, quando cancelar pedido, deve cancelar pedido")
    void dadoPedidoEmAbertoQuandoCancelarPedidoDeveCancelarPedido() throws Exception {
        quandoCancelarPedido("1")
                .andExpect(status().isOk());

        verify(cancelarPedidoUseCase).execute(1, TOKEN_USUARIO);
    }

    @Test
    @DisplayName("Dado pedido inexistente, quando cancelar pedido, deve retornar não encontrado")
    void dadoPedidoInexistenteQuandoCancelarPedidoDeveRetornarNaoEncontrado() throws Exception {
        dadoUseCaseDeCancelarLancando(new PedidoNaoEncontradoException("Pedido não encontrado"));

        quandoCancelarPedido("1")
                .andExpect(status().isNotFound())
                .andExpect(content().string("Pedido não encontrado"));
    }

    @Test
    @DisplayName("Dado pedido que não está em aberto, quando cancelar pedido, deve retornar erro de negócio")
    void dadoPedidoQueNaoEstaEmAbertoQuandoCancelarPedidoDeveRetornarErroDeNegocio() throws Exception {
        dadoUseCaseDeCancelarLancando(new BusinessException("O pedido precisa estar em aberto para ser cancelado"));

        quandoCancelarPedido("1")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("O pedido precisa estar em aberto para ser cancelado"));
    }

    @Test
    @DisplayName("Dado identificador não numérico, quando cancelar pedido, deve retornar requisição inválida")
    void dadoIdentificadorNaoNumericoQuandoCancelarPedidoDeveRetornarRequisicaoInvalida() throws Exception {
        quandoCancelarPedido("abc")
                .andExpect(status().isBadRequest());

        verify(cancelarPedidoUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("Dado erro inesperado, quando cancelar pedido, deve retornar erro interno")
    void dadoErroInesperadoQuandoCancelarPedidoDeveRetornarErroInterno() throws Exception {
        dadoUseCaseDeCancelarLancando(new IllegalStateException("falha de banco"));

        quandoCancelarPedido("1")
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(MENSAGEM_ERRO_INESPERADO));
    }

    @Test
    @DisplayName("Dado conflito de atualização concorrente, quando cancelar pedido, deve retornar conflito")
    void dadoConflitoDeAtualizacaoQuandoCancelarPedidoDeveRetornarConflito() throws Exception {
        dadoUseCaseDeCancelarLancando(new ObjectOptimisticLockingFailureException(Pedido.class, 1));

        quandoCancelarPedido("1")
                .andExpect(status().isConflict())
                .andExpect(content().string("O recurso foi alterado por outra operação, tente novamente"));
    }

    @Test
    @DisplayName("Dado conflito de atualização concorrente, quando concluir pedido, deve retornar conflito")
    void dadoConflitoDeAtualizacaoQuandoConcluirPedidoDeveRetornarConflito() throws Exception {
        dadoUseCaseDeConcluirLancando(new ObjectOptimisticLockingFailureException(Pedido.class, 1));

        quandoConcluirPedido("1")
                .andExpect(status().isConflict())
                .andExpect(content().string("O recurso foi alterado por outra operação, tente novamente"));
    }

    @Test
    @DisplayName("Dado pedido em aberto, quando concluir pedido, deve concluir pedido")
    void dadoPedidoEmAbertoQuandoConcluirPedidoDeveConcluirPedido() throws Exception {
        quandoConcluirPedido("1")
                .andExpect(status().isOk());

        verify(concluirPedidoUseCase).execute(1, TOKEN_USUARIO);
    }

    @Test
    @DisplayName("Dado pedido inexistente, quando concluir pedido, deve retornar não encontrado")
    void dadoPedidoInexistenteQuandoConcluirPedidoDeveRetornarNaoEncontrado() throws Exception {
        dadoUseCaseDeConcluirLancando(new PedidoNaoEncontradoException("Pedido não encontrado"));

        quandoConcluirPedido("1")
                .andExpect(status().isNotFound())
                .andExpect(content().string("Pedido não encontrado"));
    }

    @Test
    @DisplayName("Dado pedido que não está em aberto, quando concluir pedido, deve retornar erro de negócio")
    void dadoPedidoQueNaoEstaEmAbertoQuandoConcluirPedidoDeveRetornarErroDeNegocio() throws Exception {
        dadoUseCaseDeConcluirLancando(new BusinessException("O pedido precisa estar em aberto para ser concluído"));

        quandoConcluirPedido("1")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("O pedido precisa estar em aberto para ser concluído"));
    }

    @Test
    @DisplayName("Dado identificador não numérico, quando concluir pedido, deve retornar requisição inválida")
    void dadoIdentificadorNaoNumericoQuandoConcluirPedidoDeveRetornarRequisicaoInvalida() throws Exception {
        quandoConcluirPedido("abc")
                .andExpect(status().isBadRequest());

        verify(concluirPedidoUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("Dado erro inesperado, quando concluir pedido, deve retornar erro interno")
    void dadoErroInesperadoQuandoConcluirPedidoDeveRetornarErroInterno() throws Exception {
        dadoUseCaseDeConcluirLancando(new IllegalStateException("falha de banco"));

        quandoConcluirPedido("1")
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(MENSAGEM_ERRO_INESPERADO));
    }

    @Test
    @DisplayName("Dado token de usuário ausente, quando cancelar pedido, deve retornar requisição inválida")
    void dadoTokenUsuarioAusenteQuandoCancelarPedidoDeveRetornarRequisicaoInvalida() throws Exception {
        mockMvc.perform(patch("/pedido/{id}/cancelar", "1"))
                .andExpect(status().isBadRequest());

        verify(cancelarPedidoUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("Dado token de usuário ausente, quando concluir pedido, deve retornar requisição inválida")
    void dadoTokenUsuarioAusenteQuandoConcluirPedidoDeveRetornarRequisicaoInvalida() throws Exception {
        mockMvc.perform(patch("/pedido/{id}/concluir", "1"))
                .andExpect(status().isBadRequest());

        verify(concluirPedidoUseCase, never()).execute(any(), any());
    }

    private CriarPedidoForm dadoFormularioValido() {
        return dadoFormularioValido(
                "token-valido",
                1,
                2
        );
    }

    private CriarPedidoForm dadoFormularioValido(
            String tokenUsuario,
            Integer menuItemId,
            Integer quantidade
    ) {
        return new CriarPedidoForm(
                tokenUsuario,
                menuItemId,
                quantidade
        );
    }

    private ResultActions quandoRealizarPedido(CriarPedidoForm form) throws Exception {
        return mockMvc.perform(
                post("/pedido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))
        );
    }

    private void deveNaoCriarPedido() {
        verify(criarPedidoUseCase, never()).execute(any());
    }

    private void dadoPedidoCriadoPeloUseCase() {
        when(criarPedidoUseCase.execute(any(CriarPedidoCommand.class)))
                .thenReturn(new CriarPedidoResult(10, "Maria", "Pizza", new BigDecimal("25.90"), 2));
    }

    private void dadoUseCaseDeCriarLancando(RuntimeException exception) {
        when(criarPedidoUseCase.execute(any(CriarPedidoCommand.class))).thenThrow(exception);
    }

    private void dadoUseCaseDeCancelarLancando(RuntimeException exception) {
        doThrow(exception).when(cancelarPedidoUseCase).execute(any(), any());
    }

    private void dadoUseCaseDeConcluirLancando(RuntimeException exception) {
        doThrow(exception).when(concluirPedidoUseCase).execute(any(), any());
    }

    private ResultActions quandoRealizarPedidoSemCorpo() throws Exception {
        return mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions quandoCancelarPedido(String id) throws Exception {
        return mockMvc.perform(patch("/pedido/{id}/cancelar", id).header("Token-Usuario", TOKEN_USUARIO));
    }

    private ResultActions quandoConcluirPedido(String id) throws Exception {
        return mockMvc.perform(patch("/pedido/{id}/concluir", id).header("Token-Usuario", TOKEN_USUARIO));
    }
}
