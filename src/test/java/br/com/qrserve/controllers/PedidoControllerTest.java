package br.com.qrserve.controllers;

import br.com.qrserve.models.dto.form.CriarPedidoForm;
import br.com.qrserve.services.PedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    @DisplayName("Dado formulário válido, quando realizar pedido, deve criar pedido")
    void dadoFormularioValidoQuandoRealizarPedidoDeveCriarPedido() throws Exception {
        var form = dadoFormularioValido();

        quandoRealizarPedido(form);

        verify(pedidoService).criarPedido(any(CriarPedidoForm.class));
    }

    @Test
    @DisplayName("Dado token de usuário nulo, quando realizar pedido, deve retornar erro de validação")
    void dadoTokenUsuarioNuloQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido(null, 1, new BigDecimal("25.90"), 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tokenUsuario")
                        .value("O token de usuário é obrigatório para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado token de usuário vazio, quando realizar pedido, deve retornar erro de validação")
    void dadoTokenUsuarioVazioQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("", 1, new BigDecimal("25.90"), 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tokenUsuario")
                        .value("O token de usuário é obrigatório para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado identificador do item de menu nulo, quando realizar pedido, deve retornar erro de validação")
    void dadoMenuItemIdNuloQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", null, new BigDecimal("25.90"), 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.menuItemId")
                        .value("O identificador do item de menu é obrigatório para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado valor unitário nulo, quando realizar pedido, deve retornar erro de validação")
    void dadoValorUnitarioNuloQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", 1, null, 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valorUnitario")
                        .value("O valor unitário é obrigatório para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado valor unitário igual a zero, quando realizar pedido, deve retornar erro de validação")
    void dadoValorUnitarioIgualAZeroQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", 1, BigDecimal.ZERO, 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valorUnitario")
                        .value("O valor unitário deve ser maior que zero para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado valor unitário negativo, quando realizar pedido, deve retornar erro de validação")
    void dadoValorUnitarioNegativoQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", 1, new BigDecimal("-1.00"), 2);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valorUnitario")
                        .value("O valor unitário deve ser maior que zero para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado quantidade nula, quando realizar pedido, deve retornar erro de validação")
    void dadoQuantidadeNulaQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", 1, new BigDecimal("25.90"), null);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantidade")
                        .value("A quantidade de itens é obrigatória para criar pedido"));

        deveNaoCriarPedido();
    }

    @Test
    @DisplayName("Dado quantidade igual a zero, quando realizar pedido, deve retornar erro de validação")
    void dadoQuantidadeIgualAZeroQuandoRealizarPedidoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioValido("token-valido", 1, new BigDecimal("25.90"), 0);

        quandoRealizarPedido(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantidade")
                        .value("A quantidade de itens deve ser maior que zero para criar pedido"));

        deveNaoCriarPedido();
    }

    private CriarPedidoForm dadoFormularioValido() {
        return dadoFormularioValido(
                "token-valido",
                1,
                new BigDecimal("25.90"),
                2
        );
    }

    private CriarPedidoForm dadoFormularioValido(
            String tokenUsuario,
            Integer menuItemId,
            BigDecimal valorUnitario,
            Integer quantidade
    ) {
        return new CriarPedidoForm(
                tokenUsuario,
                menuItemId,
                valorUnitario,
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
        verify(pedidoService, never()).criarPedido(any());
    }
}
