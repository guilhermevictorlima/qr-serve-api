package br.com.qrserve.presentation.mesa;

import br.com.qrserve.domain.mesa.Mesa;
import br.com.qrserve.infrastructure.persistence.mesa.MesaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MesaController.class)
class MesaControllerTest {

    private static final String MENSAGEM_ERRO_INESPERADO = "Ocorreu um erro inesperado, tente novamente mais tarde";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MesaRepository mesaRepository;

    @Test
    @DisplayName("Dado mesas cadastradas, quando listar mesas, deve retornar as mesas")
    void dadoMesasCadastradasQuandoListarMesasDeveRetornarAsMesas() throws Exception {
        dadoMesasCadastradas(dadoMesa(1, 10), dadoMesa(2, 11));

        quandoListarMesas()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].numeroMesa").value(10))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].numeroMesa").value(11));
    }

    @Test
    @DisplayName("Dado nenhuma mesa cadastrada, quando listar mesas, deve retornar lista vazia")
    void dadoNenhumaMesaCadastradaQuandoListarMesasDeveRetornarListaVazia() throws Exception {
        dadoMesasCadastradas();

        quandoListarMesas()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Dado erro inesperado, quando listar mesas, deve retornar erro interno")
    void dadoErroInesperadoQuandoListarMesasDeveRetornarErroInterno() throws Exception {
        dadoErroInesperadoNoRepositorio();

        quandoListarMesas()
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ERRO_INESPERADO));
    }

    private Mesa dadoMesa(Integer id, Integer numeroMesa) {
        Mesa mesa = new Mesa(id);
        mesa.setNumeroMesa(numeroMesa);
        return mesa;
    }

    private void dadoMesasCadastradas(Mesa... mesas) {
        when(mesaRepository.findAll()).thenReturn(List.of(mesas));
    }

    private void dadoErroInesperadoNoRepositorio() {
        when(mesaRepository.findAll()).thenThrow(new IllegalStateException("falha de banco"));
    }

    private ResultActions quandoListarMesas() throws Exception {
        return mockMvc.perform(get("/mesas"));
    }
}
