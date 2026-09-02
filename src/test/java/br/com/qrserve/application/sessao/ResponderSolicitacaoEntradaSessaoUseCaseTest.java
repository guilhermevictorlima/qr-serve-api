package br.com.qrserve.application.sessao;

import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.domain.sessao.TokenUsuario;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import br.com.qrserve.presentation.sessao.form.ResponderSolicitacaoEntradaSessaoForm;
import br.com.qrserve.presentation.sessao.form.RespostaSolicitacaoEntradaSessao;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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

    @Captor
    private ArgumentCaptor<ParticipanteSessao> participanteArgumentCaptor;

    @InjectMocks
    private ResponderSolicitacaoEntradaSessaoUseCase useCase;

    private String tokenSolicitante;
    private String tokenRespondente;

    private ResponderSolicitacaoEntradaSessaoForm form;
    private SolicitacaoEntradaSessao solicitacao;
    private ParticipanteSessao participanteRespondente;
    private Exception exception;
    private String mensagemEsperadaException;

    @Test
    @DisplayName("Quando a solicitação for aprovada, adicionar o solicitante à sessão")
    void cenario_1() {
        dadoSolicitacaoDeEntrada();
        dadoParticipanteRespondente();
        dadoRespostaAprovada();
        quandoResponderSolicitacao();
        deveAdicionarSolicitanteComoParticipante();
        deveExcluirSolicitacao();
    }

    @Test
    @DisplayName("Quando a solicitação for recusada, notificar o solicitante")
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
    @DisplayName("Quando o participante respondente não existir, rejeitar a resposta")
    void cenario_4() {
        dadoSolicitacaoDeEntrada();
        dadoParticipanteRespondenteInexistente();
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

        return new SolicitacaoEntradaSessao(new SessaoMesa(1), token, "Fulano", LocalDateTime.of(2026, 9, 1, 13, 2, 15));
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
        form = new ResponderSolicitacaoEntradaSessaoForm(
                tokenSolicitante,
                tokenRespondente,
                RespostaSolicitacaoEntradaSessao.SOLICITACAO_APROVADA
        );
    }

    private void dadoRespostaRecusada() {
        form = new ResponderSolicitacaoEntradaSessaoForm(
                tokenSolicitante,
                tokenRespondente,
                RespostaSolicitacaoEntradaSessao.SOLICITACAO_NEGADA
        );
    }

    private void dadoSolicitacaoInexistente() {
        when(solicitacaoEntradaSessaoRepository
                .obterSolicitacaoPorToken(tokenSolicitante))
                .thenReturn(Optional.empty());

        mensagemEsperadaException = "Solicitação de entrada não encontrada. Verifique se já foi respondida e tente novamente";
    }

    private void dadoParticipanteRespondenteInexistente() {
        when(participanteSessaoRepository
                .obterParticipantePorToken(tokenRespondente))
                .thenReturn(Optional.empty());

        mensagemEsperadaException = "Participante respondente não encontrado, token identificador inválido";
    }

    private void quandoResponderSolicitacao() {
        try {
            useCase.execute(form);
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
        assertEquals(mensagemEsperadaException, exception.getMessage());

        verify(participanteSessaoRepository, never())
                .save(any(ParticipanteSessao.class));

        verify(solicitacaoEntradaSessaoRepository, never())
                .delete(any(SolicitacaoEntradaSessao.class));

        verify(feedbackSender, never()).notificar();
    }

}