package br.com.qrserve.presentation.participantesessao;

import br.com.qrserve.application.sessao.ListarParticipantesCommand;
import br.com.qrserve.application.sessao.ListarParticipantesUseCase;
import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.TokenUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipanteSessaoController.class)
class ParticipanteSessaoControllerTest {

    private static final String MENSAGEM_ERRO_INESPERADO = "Ocorreu um erro inesperado, tente novamente mais tarde";
    private static final String MENSAGEM_PARTICIPANTE_NAO_ENCONTRADO = "Participante não encontrado, token identificador inválido";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListarParticipantesUseCase listarParticipantesUseCase;

    private TokenUsuario tokenMaria;
    private TokenUsuario tokenJoao;

    @Test
    @DisplayName("@spec:AC-012 Dado sessão com participantes, quando listar participantes, deve retornar os participantes")
    void dadoSessaoComParticipantesQuandoListarParticipantesDeveRetornarOsParticipantes() throws Exception {
        dadoParticipantesNaSessao(5);

        quandoListarParticipantes("5", "token-maria")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sessaoMesaId").value(5))
                .andExpect(jsonPath("$[0].token").value(tokenMaria.getValor()))
                .andExpect(jsonPath("$[0].nome").value("Maria"))
                .andExpect(jsonPath("$[1].sessaoMesaId").value(5))
                .andExpect(jsonPath("$[1].token").value(tokenJoao.getValor()))
                .andExpect(jsonPath("$[1].nome").value("João"));

        deveConsultarParticipantesDaSessao(5, "token-maria");
    }

    @Test
    @DisplayName("@spec:AC-013 Dado consultante que não participa da sessão, quando listar participantes, deve retornar não encontrado")
    void dadoConsultanteForaDaSessaoQuandoListarParticipantesDeveRetornarNaoEncontrado() throws Exception {
        dadoConsultanteNaoParticipante();

        quandoListarParticipantes("5", "token-invalido")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_PARTICIPANTE_NAO_ENCONTRADO));
    }

    @Test
    @DisplayName("Dado requisição sem token do usuário, quando listar participantes, deve retornar requisição inválida")
    void dadoRequisicaoSemTokenQuandoListarParticipantesDeveRetornarRequisicaoInvalida() throws Exception {
        mockMvc.perform(get("/participantes/{sessaoId}", "5"))
                .andExpect(status().isBadRequest());

        verify(listarParticipantesUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("Dado identificador de sessão não numérico, quando listar participantes, deve retornar requisição inválida")
    void dadoIdentificadorDeSessaoNaoNumericoQuandoListarParticipantesDeveRetornarRequisicaoInvalida() throws Exception {
        quandoListarParticipantes("abc", "token-maria")
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Dado erro inesperado, quando listar participantes, deve retornar erro interno")
    void dadoErroInesperadoQuandoListarParticipantesDeveRetornarErroInterno() throws Exception {
        dadoErroInesperadoNoUseCase();

        quandoListarParticipantes("5", "token-maria")
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ERRO_INESPERADO));
    }

    private void dadoParticipantesNaSessao(Integer sessaoId) {
        SessaoMesa sessao = new SessaoMesa(sessaoId);
        tokenMaria = TokenUsuario.create();
        tokenJoao = TokenUsuario.create();

        when(listarParticipantesUseCase.execute(any(ListarParticipantesCommand.class))).thenReturn(List.of(
                new ParticipanteSessao(sessao, tokenMaria, "Maria"),
                new ParticipanteSessao(sessao, tokenJoao, "João")
        ));
    }

    private void dadoConsultanteNaoParticipante() {
        when(listarParticipantesUseCase.execute(any(ListarParticipantesCommand.class)))
                .thenThrow(new ParticipanteSessaoNaoEncontradoException(MENSAGEM_PARTICIPANTE_NAO_ENCONTRADO));
    }

    private void dadoErroInesperadoNoUseCase() {
        when(listarParticipantesUseCase.execute(any(ListarParticipantesCommand.class)))
                .thenThrow(new IllegalStateException("falha de banco"));
    }

    private ResultActions quandoListarParticipantes(String sessaoId, String tokenUsuario) throws Exception {
        return mockMvc.perform(
                get("/participantes/{sessaoId}", sessaoId)
                        .header("X-Token-Usuario", tokenUsuario)
        );
    }

    private void deveConsultarParticipantesDaSessao(Integer sessaoId, String tokenUsuario) {
        verify(listarParticipantesUseCase)
                .execute(new ListarParticipantesCommand(sessaoId, tokenUsuario));
    }
}
