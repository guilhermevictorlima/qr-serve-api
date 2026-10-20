package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.EstadoInvalidoException;
import br.com.qrserve.domain.exception.SolicitacaoEntradaSessaoNaoEncontradaException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponderSolicitacaoEntradaSessaoUseCaseTest {

    @Mock
    private SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;

    @Mock
    private ParticipanteSessaoRepository participanteSessaoRepository;

    @Mock
    private FeedbackSender feedbackSender;

    @Mock
    private TimeProvider timeProvider;

    @Captor
    private ArgumentCaptor<ParticipanteSessao> participanteArgumentCaptor;

    @InjectMocks
    private ResponderSolicitacaoEntradaSessaoUseCase useCase;

    private static final LocalDateTime DATA_HORA_SOLICITACAO = LocalDateTime.of(2026, 9, 1, 13, 2, 15);

    private String tokenSolicitante;
    private String tokenRespondente;

    private ResponderSolicitacaoEntradaSessaoCommand command;
    private SolicitacaoEntradaSessao solicitacao;
    private ParticipanteSessao participanteRespondente;
    private Exception exception;
    private Class<? extends Exception> tipoEsperadoException;
    private String mensagemEsperadaException;

    @BeforeEach
    void setUp() {
        lenient().when(timeProvider.dataHoraAtual()).thenReturn(DATA_HORA_SOLICITACAO.plusMinutes(1));
    }

    @Test
    @DisplayName("@spec:AC-010 Quando a solicitação estiver expirada, rejeitar a resposta")
    void cenario_6() {
        dadoSolicitacaoDeEntrada();
        dadoParticipanteRespondente();
        dadoRespostaAprovada();
        dadoSolicitacaoExpirada();
        quandoResponderSolicitacao();
        deveRejeitarResposta();
    }

    @Test
    @DisplayName("@spec:AC-007Quando a solicitação for aprovada, adicionar o solicitante à sessão")
    void cenario_1() {
        dadoSolicitacaoDeEntrada();
        dadoParticipanteRespondente();
        dadoRespostaAprovada();
        quandoResponderSolicitacao();
        deveAdicionarSolicitanteComoParticipante();
        deveExcluirSolicitacao();
    }

    @Test
    @DisplayName("@spec:AC-008 Quando a solicitação for recusada, notificar o solicitante")
    void cenario_2() {
        dadoSolicitacaoDeEntrada();
        dadoParticipanteRespondente();
        dadoRespostaRecusada();
        quandoResponderSolicitacao();
        deveNotificarSolicitante();
        deveExcluirSolicitacao();
    }

    @Test
    @DisplayName("Quando a solicitação não existir, rejeitar a resposta")
    void cenario_3() {
        dadoSolicitacaoInexistente();
        dadoRespostaAprovada();
        quandoResponderSolicitacao();
        deveRejeitarResposta();
    }

    @Test
    @DisplayName("@spec:AC-009 Quando o participante respondente não existir, rejeitar a resposta")
    void cenario_4() {
        dadoSolicitacaoDeEntrada();
        dadoParticipanteRespondenteInexistente();
        dadoRespostaAprovada();
        quandoResponderSolicitacao();
        deveRejeitarResposta();
    }

    @Test
    @DisplayName("@spec:AC-009 Quando o participante respondente for de outra sessão, rejeitar a resposta")
    void cenario_5() {
        dadoSolicitacaoDeEntrada();
        dadoParticipanteRespondenteDeOutraSessao();
        dadoRespostaAprovada();
        quandoResponderSolicitacao();
        deveRejeitarResposta();
    }

    private void dadoSolicitacaoDeEntrada() {
        solicitacao = criarSolicitacao();

        when(solicitacaoEntradaSessaoRepository
                .obterSolicitacaoPorToken(tokenSolicitante))
                .thenReturn(Optional.of(solicitacao));
    }

    private SolicitacaoEntradaSessao criarSolicitacao() {
        TokenUsuario token = TokenUsuario.create();
        tokenSolicitante = token.getValor();

        return new SolicitacaoEntradaSessao(new SessaoMesa(1), token, "Fulano", DATA_HORA_SOLICITACAO);
    }

    private void dadoParticipanteRespondente() {
        participanteRespondente = criarParticipanteRespondente();

        when(participanteSessaoRepository
                .obterParticipantePorToken(tokenRespondente))
                .thenReturn(Optional.of(participanteRespondente));
    }

    private ParticipanteSessao criarParticipanteRespondente() {
        TokenUsuario token = TokenUsuario.create();
        tokenRespondente = token.getValor();

        return new ParticipanteSessao(new SessaoMesa(1), token, "Ciclano");
    }

    private void dadoRespostaAprovada() {
        command = new ResponderSolicitacaoEntradaSessaoCommand(
                tokenSolicitante,
                tokenRespondente,
                RespostaSolicitacao.APROVADA
        );
    }

    private void dadoRespostaRecusada() {
        command = new ResponderSolicitacaoEntradaSessaoCommand(
                tokenSolicitante,
                tokenRespondente,
                RespostaSolicitacao.NEGADA
        );
    }

    private void dadoSolicitacaoExpirada() {
        when(timeProvider.dataHoraAtual())
                .thenReturn(DATA_HORA_SOLICITACAO.plusMinutes(3).plusSeconds(1));

        mensagemEsperadaException = "Solicitação de entrada expirada. Escaneie o QR Code novamente";
        tipoEsperadoException = EstadoInvalidoException.class;
    }

    private void dadoSolicitacaoInexistente() {
        when(solicitacaoEntradaSessaoRepository
                .obterSolicitacaoPorToken(tokenSolicitante))
                .thenReturn(Optional.empty());

        mensagemEsperadaException = "Solicitação de entrada não encontrada. Verifique se já foi respondida e tente novamente";
        tipoEsperadoException = SolicitacaoEntradaSessaoNaoEncontradaException.class;
    }

    private void dadoParticipanteRespondenteInexistente() {
        when(participanteSessaoRepository
                .obterParticipantePorToken(tokenRespondente))
                .thenReturn(Optional.empty());

        mensagemEsperadaException = "Participante respondente não encontrado, token identificador inválido";
        tipoEsperadoException = ParticipanteSessaoNaoEncontradoException.class;
    }

    private void dadoParticipanteRespondenteDeOutraSessao() {
        TokenUsuario token = TokenUsuario.create();
        tokenRespondente = token.getValor();

        when(participanteSessaoRepository
                .obterParticipantePorToken(tokenRespondente))
                .thenReturn(Optional.of(new ParticipanteSessao(new SessaoMesa(2), token, "Ciclano")));

        mensagemEsperadaException = "Participante respondente não encontrado, token identificador inválido";
        tipoEsperadoException = ParticipanteSessaoNaoEncontradoException.class;
    }

    private void quandoResponderSolicitacao() {
        try {
            useCase.execute(command);
        } catch (Exception e) {
            exception = e;
        }
    }

    private void deveAdicionarSolicitanteComoParticipante() {
        verify(participanteSessaoRepository)
                .save(participanteArgumentCaptor.capture());

        ParticipanteSessao participante = participanteArgumentCaptor.getValue();

        assertEquals(solicitacao.getSessao().getId(), participante.getSessao().getId());
        assertEquals(solicitacao.getNome(), participante.getNome());
        assertEquals(solicitacao.getToken(), participante.getToken());
    }

    private void deveNotificarSolicitante() {
        verify(feedbackSender).notificar();
    }

    private void deveExcluirSolicitacao() {
        verify(solicitacaoEntradaSessaoRepository)
                .delete(solicitacao);
    }

    private void deveRejeitarResposta() {
        assertNotNull(exception);
        assertInstanceOf(tipoEsperadoException, exception);
        assertEquals(mensagemEsperadaException, exception.getMessage());

        verify(participanteSessaoRepository, never())
                .save(any(ParticipanteSessao.class));

        verify(solicitacaoEntradaSessaoRepository, never())
                .delete(any(SolicitacaoEntradaSessao.class));

        verify(feedbackSender, never()).notificar();
    }

}