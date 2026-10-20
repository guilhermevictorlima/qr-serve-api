package br.com.qrserve.presentation.participantesessao;

import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.TokenUsuario;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipanteSessaoController.class)
class ParticipanteSessaoControllerTest {

    private static final String MENSAGEM_ERRO_INESPERADO = "Ocorreu um erro inesperado, tente novamente mais tarde";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParticipanteSessaoRepository participanteSessaoRepository;

    private TokenUsuario tokenMaria;
    private TokenUsuario tokenJoao;

    @Test
    @DisplayName("Dado sessão com participantes, quando listar participantes, deve retornar os participantes")
    void dadoSessaoComParticipantesQuandoListarParticipantesDeveRetornarOsParticipantes() throws Exception {
        dadoParticipantesNaSessao(5);

        quandoListarParticipantes("5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sessaoMesaId").value(5))
                .andExpect(jsonPath("$[0].token").value(tokenMaria.getValor()))
                .andExpect(jsonPath("$[0].nome").value("Maria"))
                .andExpect(jsonPath("$[1].sessaoMesaId").value(5))
                .andExpect(jsonPath("$[1].token").value(tokenJoao.getValor()))
                .andExpect(jsonPath("$[1].nome").value("João"));

        deveConsultarParticipantesDaSessao(5);
    }

    @Test
    @DisplayName("Dado sessão sem participantes, quando listar participantes, deve retornar lista vazia")
    void dadoSessaoSemParticipantesQuandoListarParticipantesDeveRetornarListaVazia() throws Exception {
        dadoSessaoSemParticipantes(5);

        quandoListarParticipantes("5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Dado identificador de sessão não numérico, quando listar participantes, deve retornar requisição inválida")
    void dadoIdentificadorDeSessaoNaoNumericoQuandoListarParticipantesDeveRetornarRequisicaoInvalida() throws Exception {
        quandoListarParticipantes("abc")
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Dado erro inesperado, quando listar participantes, deve retornar erro interno")
    void dadoErroInesperadoQuandoListarParticipantesDeveRetornarErroInterno() throws Exception {
        dadoErroInesperadoNoRepositorio();

        quandoListarParticipantes("5")
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ERRO_INESPERADO));
    }

    private void dadoParticipantesNaSessao(Integer sessaoId) {
        SessaoMesa sessao = new SessaoMesa(sessaoId);
        tokenMaria = TokenUsuario.create();
        tokenJoao = TokenUsuario.create();

        when(participanteSessaoRepository.listarParticipantes(sessaoId)).thenReturn(List.of(
                new ParticipanteSessao(sessao, tokenMaria, "Maria"),
                new ParticipanteSessao(sessao, tokenJoao, "João")
        ));
    }

    private void dadoSessaoSemParticipantes(Integer sessaoId) {
        when(participanteSessaoRepository.listarParticipantes(sessaoId)).thenReturn(List.of());
    }

    private void dadoErroInesperadoNoRepositorio() {
        when(participanteSessaoRepository.listarParticipantes(5)).thenThrow(new IllegalStateException("falha de banco"));
    }

    private ResultActions quandoListarParticipantes(String sessaoId) throws Exception {
        return mockMvc.perform(get("/participantes/{sessaoId}", sessaoId));
    }

    private void deveConsultarParticipantesDaSessao(Integer sessaoId) {
        verify(participanteSessaoRepository).listarParticipantes(sessaoId);
    }
}
