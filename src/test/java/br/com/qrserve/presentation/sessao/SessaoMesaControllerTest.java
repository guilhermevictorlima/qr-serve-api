package br.com.qrserve.presentation.sessao;

import br.com.qrserve.application.sessao.AcessarSessaoUseCase;
import br.com.qrserve.application.sessao.ListarSolicitacoesPendentesCommand;
import br.com.qrserve.application.sessao.ListarSolicitacoesPendentesUseCase;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.domain.sessao.TokenUsuario;
import java.time.LocalDateTime;
import java.util.List;
import br.com.qrserve.application.sessao.ResponderSolicitacaoEntradaSessaoUseCase;
import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.exception.SolicitacaoEntradaSessaoNaoEncontradaException;
import br.com.qrserve.presentation.sessao.form.AcessarSessaoMesaForm;
import br.com.qrserve.presentation.sessao.form.RespostaSolicitacaoEntradaSessao;
import br.com.qrserve.presentation.sessao.form.ResponderSolicitacaoEntradaSessaoForm;
import br.com.qrserve.application.sessao.AcessarSessaoCommand;
import br.com.qrserve.application.sessao.AcessarSessaoResult;
import br.com.qrserve.application.sessao.ResponderSolicitacaoEntradaSessaoCommand;
import br.com.qrserve.application.sessao.RespostaSolicitacao;
import br.com.qrserve.application.sessao.StatusAcessoSessao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessaoMesaController.class)
class SessaoMesaControllerTest {

    private static final String MENSAGEM_ERRO_INESPERADO = "Ocorreu um erro inesperado, tente novamente mais tarde";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AcessarSessaoUseCase acessarSessaoUseCase;

    @MockitoBean
    private ResponderSolicitacaoEntradaSessaoUseCase responderSolicitacaoEntradaSessaoUseCase;

    @MockitoBean
    private ListarSolicitacoesPendentesUseCase listarSolicitacoesPendentesUseCase;

    private TokenUsuario tokenSolicitante;

    @Test
    @DisplayName("Dado formulário válido, quando acessar sessão, deve acessar sessão")
    void dadoFormularioValidoQuandoAcessarSessaoDeveAcessarSessao() throws Exception {
        var form = dadoFormularioDeAcessoValido();

        quandoAcessarSessao(form);

        verify(acessarSessaoUseCase).execute(new AcessarSessaoCommand(1, "Maria"));
    }

    @Test
    @DisplayName("Dado formulário válido, quando acessar sessão, deve retornar token e status do acesso")
    void dadoFormularioValidoQuandoAcessarSessaoDeveRetornarTokenEStatusDoAcesso() throws Exception {
        var form = dadoFormularioDeAcessoValido();
        dadoAcessoLiberadoPeloUseCase();

        quandoAcessarSessao(form)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenUsuario").value("token-usuario"))
                .andExpect(jsonPath("$.sessaoMesaId").value(5))
                .andExpect(jsonPath("$.responseStatus").value("ACESSO_LIBERADO"));
    }

    @Test
    @DisplayName("Dado identificador da mesa nulo, quando acessar sessão, deve retornar erro de validação")
    void dadoMesaIdNuloQuandoAcessarSessaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeAcesso(null, "Maria");

        quandoAcessarSessao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.mesaId")
                        .value("O identificador da mesa é obrigatório para acessar a sessão"));

        deveNaoAcessarSessao();
    }

    @Test
    @DisplayName("Dado nome nulo, quando acessar sessão, deve retornar erro de validação")
    void dadoNomeNuloQuandoAcessarSessaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeAcesso(1, null);

        quandoAcessarSessao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome")
                        .value("O nome é obrigatório para acessar a sessão"));

        deveNaoAcessarSessao();
    }

    @Test
    @DisplayName("Dado nome vazio, quando acessar sessão, deve retornar erro de validação")
    void dadoNomeVazioQuandoAcessarSessaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeAcesso(1, "");

        quandoAcessarSessao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome")
                        .value("O nome é obrigatório para acessar a sessão"));

        deveNaoAcessarSessao();
    }

    @Test
    @DisplayName("Dado nome apenas com espaços, quando acessar sessão, deve retornar erro de validação")
    void dadoNomeApenasComEspacosQuandoAcessarSessaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeAcesso(1, "   ");

        quandoAcessarSessao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome")
                        .value("O nome é obrigatório para acessar a sessão"));

        deveNaoAcessarSessao();
    }

    @Test
    @DisplayName("Dado corpo da requisição ausente, quando acessar sessão, deve retornar requisição inválida")
    void dadoCorpoAusenteQuandoAcessarSessaoDeveRetornarRequisicaoInvalida() throws Exception {
        quandoAcessarSessaoSemCorpo()
                .andExpect(status().isBadRequest());

        deveNaoAcessarSessao();
    }

    @Test
    @DisplayName("Dado erro inesperado, quando acessar sessão, deve retornar erro interno")
    void dadoErroInesperadoQuandoAcessarSessaoDeveRetornarErroInterno() throws Exception {
        var form = dadoFormularioDeAcessoValido();
        dadoUseCaseDeAcessarLancando(new IllegalStateException("falha de banco"));

        quandoAcessarSessao(form)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ERRO_INESPERADO));
    }

    @Test
    @DisplayName("Dado formulário válido, quando responder solicitação, deve responder solicitação")
    void dadoFormularioValidoQuandoResponderSolicitacaoDeveResponderSolicitacao() throws Exception {
        var form = dadoFormularioDeRespostaValido();

        quandoResponderSolicitacao(form)
                .andExpect(status().isOk());

        verify(responderSolicitacaoEntradaSessaoUseCase).execute(
                new ResponderSolicitacaoEntradaSessaoCommand("token-solicitante", "token-respondente", RespostaSolicitacao.APROVADA));
    }

    @Test
    @DisplayName("Dado token do solicitante nulo, quando responder solicitação, deve retornar erro de validação")
    void dadoTokenSolicitanteNuloQuandoResponderSolicitacaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeResposta(null, "token-respondente", RespostaSolicitacaoEntradaSessao.SOLICITACAO_APROVADA);

        quandoResponderSolicitacao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tokenUsuarioSolicitante")
                        .value("O token do usuário solicitante é obrigatório para responder a solicitação"));

        deveNaoResponderSolicitacao();
    }

    @Test
    @DisplayName("Dado token do solicitante vazio, quando responder solicitação, deve retornar erro de validação")
    void dadoTokenSolicitanteVazioQuandoResponderSolicitacaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeResposta("", "token-respondente", RespostaSolicitacaoEntradaSessao.SOLICITACAO_APROVADA);

        quandoResponderSolicitacao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tokenUsuarioSolicitante")
                        .value("O token do usuário solicitante é obrigatório para responder a solicitação"));

        deveNaoResponderSolicitacao();
    }

    @Test
    @DisplayName("Dado token do respondente nulo, quando responder solicitação, deve retornar erro de validação")
    void dadoTokenRespondenteNuloQuandoResponderSolicitacaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeResposta("token-solicitante", null, RespostaSolicitacaoEntradaSessao.SOLICITACAO_APROVADA);

        quandoResponderSolicitacao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tokenUsuarioRespondente")
                        .value("O token do usuário respondente é obrigatório para responder a solicitação"));

        deveNaoResponderSolicitacao();
    }

    @Test
    @DisplayName("Dado token do respondente vazio, quando responder solicitação, deve retornar erro de validação")
    void dadoTokenRespondenteVazioQuandoResponderSolicitacaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeResposta("token-solicitante", "", RespostaSolicitacaoEntradaSessao.SOLICITACAO_APROVADA);

        quandoResponderSolicitacao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tokenUsuarioRespondente")
                        .value("O token do usuário respondente é obrigatório para responder a solicitação"));

        deveNaoResponderSolicitacao();
    }

    @Test
    @DisplayName("Dado resposta nula, quando responder solicitação, deve retornar erro de validação")
    void dadoRespostaNulaQuandoResponderSolicitacaoDeveRetornarErroDeValidacao() throws Exception {
        var form = dadoFormularioDeResposta("token-solicitante", "token-respondente", null);

        quandoResponderSolicitacao(form)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.resposta")
                        .value("A resposta da solicitação é obrigatória para responder a solicitação"));

        deveNaoResponderSolicitacao();
    }

    @Test
    @DisplayName("Dado resposta desconhecida, quando responder solicitação, deve retornar requisição inválida")
    void dadoRespostaDesconhecidaQuandoResponderSolicitacaoDeveRetornarRequisicaoInvalida() throws Exception {
        var corpo = dadoCorpoComRespostaDesconhecida();

        quandoResponderSolicitacaoComCorpo(corpo)
                .andExpect(status().isBadRequest());

        deveNaoResponderSolicitacao();
    }

    @Test
    @DisplayName("Dado solicitação inexistente, quando responder solicitação, deve retornar não encontrado")
    void dadoSolicitacaoInexistenteQuandoResponderSolicitacaoDeveRetornarNaoEncontrado() throws Exception {
        var form = dadoFormularioDeRespostaValido();
        dadoUseCaseDeResponderLancando(new SolicitacaoEntradaSessaoNaoEncontradaException("Solicitação de entrada não encontrada"));

        quandoResponderSolicitacao(form)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Solicitação de entrada não encontrada"));
    }

    @Test
    @DisplayName("Dado respondente inexistente, quando responder solicitação, deve retornar não encontrado")
    void dadoRespondenteInexistenteQuandoResponderSolicitacaoDeveRetornarNaoEncontrado() throws Exception {
        var form = dadoFormularioDeRespostaValido();
        dadoUseCaseDeResponderLancando(new ParticipanteSessaoNaoEncontradoException("Participante respondente não encontrado"));

        quandoResponderSolicitacao(form)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Participante respondente não encontrado"));
    }

    @Test
    @DisplayName("Dado erro inesperado, quando responder solicitação, deve retornar erro interno")
    void dadoErroInesperadoQuandoResponderSolicitacaoDeveRetornarErroInterno() throws Exception {
        var form = dadoFormularioDeRespostaValido();
        dadoUseCaseDeResponderLancando(new IllegalStateException("falha de banco"));

        quandoResponderSolicitacao(form)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ERRO_INESPERADO));
    }

    private AcessarSessaoMesaForm dadoFormularioDeAcessoValido() {
        return dadoFormularioDeAcesso(1, "Maria");
    }

    private AcessarSessaoMesaForm dadoFormularioDeAcesso(Integer mesaId, String nome) {
        return new AcessarSessaoMesaForm(mesaId, nome);
    }

    private ResponderSolicitacaoEntradaSessaoForm dadoFormularioDeRespostaValido() {
        return dadoFormularioDeResposta(
                "token-solicitante",
                "token-respondente",
                RespostaSolicitacaoEntradaSessao.SOLICITACAO_APROVADA
        );
    }

    private ResponderSolicitacaoEntradaSessaoForm dadoFormularioDeResposta(
            String tokenSolicitante,
            String tokenRespondente,
            RespostaSolicitacaoEntradaSessao resposta
    ) {
        return new ResponderSolicitacaoEntradaSessaoForm(tokenSolicitante, tokenRespondente, resposta);
    }

    private String dadoCorpoComRespostaDesconhecida() {
        return """
                {
                  "tokenUsuarioSolicitante": "token-solicitante",
                  "tokenUsuarioRespondente": "token-respondente",
                  "resposta": "TALVEZ"
                }
                """;
    }

    private void dadoAcessoLiberadoPeloUseCase() {
        when(acessarSessaoUseCase.execute(any(AcessarSessaoCommand.class)))
                .thenReturn(new AcessarSessaoResult("token-usuario", 5, StatusAcessoSessao.ACESSO_LIBERADO));
    }

    private void dadoUseCaseDeAcessarLancando(RuntimeException exception) {
        when(acessarSessaoUseCase.execute(any(AcessarSessaoCommand.class))).thenThrow(exception);
    }

    private void dadoUseCaseDeResponderLancando(RuntimeException exception) {
        doThrow(exception).when(responderSolicitacaoEntradaSessaoUseCase).execute(any());
    }

    private ResultActions quandoAcessarSessao(AcessarSessaoMesaForm form) throws Exception {
        return mockMvc.perform(
                post("/sessao/acessar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))
        );
    }

    private ResultActions quandoAcessarSessaoSemCorpo() throws Exception {
        return mockMvc.perform(post("/sessao/acessar").contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions quandoResponderSolicitacao(ResponderSolicitacaoEntradaSessaoForm form) throws Exception {
        return quandoResponderSolicitacaoComCorpo(objectMapper.writeValueAsString(form));
    }

    private ResultActions quandoResponderSolicitacaoComCorpo(String corpo) throws Exception {
        return mockMvc.perform(
                patch("/sessao/solicitacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo)
        );
    }

    @Test
    @DisplayName("@spec:AC-005 Dado solicitações pendentes, quando listar solicitações, deve retornar as solicitações")
    void dadoSolicitacoesPendentesQuandoListarSolicitacoesDeveRetornarAsSolicitacoes() throws Exception {
        dadoSolicitacaoPendentePeloUseCase();

        quandoListarSolicitacoes("5", "token-participante")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tokenUsuario").value(tokenSolicitante.getValor()))
                .andExpect(jsonPath("$[0].nome").value("Fulano"));

        verify(listarSolicitacoesPendentesUseCase)
                .execute(new ListarSolicitacoesPendentesCommand(5, "token-participante"));
    }

    @Test
    @DisplayName("Dado participante inexistente, quando listar solicitações, deve retornar não encontrado")
    void dadoParticipanteInexistenteQuandoListarSolicitacoesDeveRetornarNaoEncontrado() throws Exception {
        dadoUseCaseDeListarLancando(new ParticipanteSessaoNaoEncontradoException("Participante não encontrado, token identificador inválido"));

        quandoListarSolicitacoes("5", "token-invalido")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Participante não encontrado, token identificador inválido"));
    }

    @Test
    @DisplayName("Dado requisição sem token do usuário, quando listar solicitações, deve retornar requisição inválida")
    void dadoRequisicaoSemTokenQuandoListarSolicitacoesDeveRetornarRequisicaoInvalida() throws Exception {
        mockMvc.perform(get("/sessao/5/solicitacoes"))
                .andExpect(status().isBadRequest());

        verify(listarSolicitacoesPendentesUseCase, never()).execute(any());
    }

    private void dadoSolicitacaoPendentePeloUseCase() {
        tokenSolicitante = TokenUsuario.create();

        when(listarSolicitacoesPendentesUseCase.execute(any(ListarSolicitacoesPendentesCommand.class)))
                .thenReturn(List.of(new SolicitacaoEntradaSessao(new SessaoMesa(5), tokenSolicitante, "Fulano", LocalDateTime.of(2026, 9, 1, 12, 0))));
    }

    private void dadoUseCaseDeListarLancando(RuntimeException exception) {
        when(listarSolicitacoesPendentesUseCase.execute(any(ListarSolicitacoesPendentesCommand.class))).thenThrow(exception);
    }

    private ResultActions quandoListarSolicitacoes(String sessaoId, String tokenUsuario) throws Exception {
        return mockMvc.perform(
                get("/sessao/{sessaoId}/solicitacoes", sessaoId)
                        .header("X-Token-Usuario", tokenUsuario)
        );
    }

    private void deveNaoAcessarSessao() {
        verify(acessarSessaoUseCase, never()).execute(any());
    }

    private void deveNaoResponderSolicitacao() {
        verify(responderSolicitacaoEntradaSessaoUseCase, never()).execute(any());
    }
}
