package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.domain.sessao.TokenUsuario;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarSolicitacoesPendentesUseCaseTest {
    private static final LocalDateTime DATA_HORA_ATUAL = LocalDateTime.of(2026, 9, 1, 12, 34, 50);
    private static final int SESSAO_ID = 1;

    @Mock
    private SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;

    @Mock
    private ParticipanteSessaoRepository participanteSessaoRepository;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private ListarSolicitacoesPendentesUseCase useCase;

    private String tokenParticipante;
    private SolicitacaoEntradaSessao solicitacaoPendente;
    private List<SolicitacaoEntradaSessao> response;
    private Exception exception;

    @BeforeEach
    void setUp() {
        lenient().when(timeProvider.dataHoraAtual()).thenReturn(DATA_HORA_ATUAL);
    }

    @Test
    @DisplayName("@spec:AC-005 Quando um participante consultar, retornar as solicitações pendentes da sessão")
    void cenario_1() {
        dadoParticipanteDaSessao(SESSAO_ID);
        dadoSolicitacaoPendenteNaSessao();
        quandoListar();
        deveRetornarSolicitacaoPendente();
    }

    @Test
    @DisplayName("Quando houver solicitação expirada, não retornar a solicitação")
    void cenario_2() {
        dadoParticipanteDaSessao(SESSAO_ID);
        dadoSolicitacaoExpiradaNaSessao();
        quandoListar();
        deveRetornarListaVazia();
    }

    @Test
    @DisplayName("Quando o consultante for de outra sessão, rejeitar a consulta")
    void cenario_3() {
        dadoParticipanteDaSessao(2);
        quandoListarComErro();
        deveRejeitarConsulta();
    }

    @Test
    @DisplayName("Quando o consultante não existir, rejeitar a consulta")
    void cenario_4() {
        dadoConsultanteInexistente();
        quandoListarComErro();
        deveRejeitarConsulta();
    }

    private void dadoParticipanteDaSessao(int sessaoId) {
        TokenUsuario token = TokenUsuario.create();
        tokenParticipante = token.getValor();

        when(participanteSessaoRepository.obterParticipantePorToken(tokenParticipante))
                .thenReturn(Optional.of(new ParticipanteSessao(new SessaoMesa(sessaoId), token, "Maria")));
    }

    private void dadoConsultanteInexistente() {
        tokenParticipante = TokenUsuario.create().getValor();

        when(participanteSessaoRepository.obterParticipantePorToken(tokenParticipante))
                .thenReturn(Optional.empty());
    }

    private void dadoSolicitacaoPendenteNaSessao() {
        solicitacaoPendente = criarSolicitacao(DATA_HORA_ATUAL.minusMinutes(1));

        when(solicitacaoEntradaSessaoRepository.listarPorSessao(SESSAO_ID))
                .thenReturn(List.of(solicitacaoPendente));
    }

    private void dadoSolicitacaoExpiradaNaSessao() {
        when(solicitacaoEntradaSessaoRepository.listarPorSessao(SESSAO_ID))
                .thenReturn(List.of(criarSolicitacao(DATA_HORA_ATUAL.minusMinutes(4))));
    }

    private SolicitacaoEntradaSessao criarSolicitacao(LocalDateTime dataHoraSolicitacao) {
        return new SolicitacaoEntradaSessao(new SessaoMesa(SESSAO_ID), TokenUsuario.create(), "Fulano", dataHoraSolicitacao);
    }

    private void quandoListar() {
        response = useCase.execute(new ListarSolicitacoesPendentesCommand(SESSAO_ID, tokenParticipante));
    }

    private void quandoListarComErro() {
        try {
            useCase.execute(new ListarSolicitacoesPendentesCommand(SESSAO_ID, tokenParticipante));
        } catch (Exception e) {
            exception = e;
        }
    }

    private void deveRetornarSolicitacaoPendente() {
        assertEquals(1, response.size());
        assertEquals(solicitacaoPendente, response.get(0));
    }

    private void deveRetornarListaVazia() {
        assertTrue(response.isEmpty());
    }

    private void deveRejeitarConsulta() {
        assertInstanceOf(ParticipanteSessaoNaoEncontradoException.class, exception);
        assertEquals("Participante não encontrado, token identificador inválido", exception.getMessage());
    }
}
