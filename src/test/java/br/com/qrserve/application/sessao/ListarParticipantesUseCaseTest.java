package br.com.qrserve.application.sessao;

import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.TokenUsuario;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarParticipantesUseCaseTest {
    private static final int SESSAO_ID = 1;

    @Mock
    private ParticipanteSessaoRepository participanteSessaoRepository;

    @InjectMocks
    private ListarParticipantesUseCase useCase;

    private String tokenConsultante;
    private List<ParticipanteSessao> participantesDaSessao;
    private List<ParticipanteSessao> response;
    private Exception exception;

    @Test
    @DisplayName("@spec:AC-012 Quando um participante consultar, retornar os participantes da sessão")
    void cenario_1() {
        dadoConsultanteParticipanteDaSessao(SESSAO_ID);
        dadoParticipantesNaSessao();
        quandoListar();
        deveRetornarParticipantesDaSessao();
    }

    @Test
    @DisplayName("@spec:AC-013 Quando o consultante for de outra sessão, rejeitar a consulta")
    void cenario_2() {
        dadoConsultanteParticipanteDaSessao(2);
        quandoListarComErro();
        deveRejeitarConsulta();
    }

    @Test
    @DisplayName("@spec:AC-013 Quando o consultante não existir, rejeitar a consulta")
    void cenario_3() {
        dadoConsultanteInexistente();
        quandoListarComErro();
        deveRejeitarConsulta();
    }

    private void dadoConsultanteParticipanteDaSessao(int sessaoId) {
        TokenUsuario token = TokenUsuario.create();
        tokenConsultante = token.getValor();

        when(participanteSessaoRepository.obterParticipantePorToken(tokenConsultante))
                .thenReturn(Optional.of(new ParticipanteSessao(new SessaoMesa(sessaoId), token, "Maria")));
    }

    private void dadoConsultanteInexistente() {
        tokenConsultante = TokenUsuario.create().getValor();

        when(participanteSessaoRepository.obterParticipantePorToken(tokenConsultante))
                .thenReturn(Optional.empty());
    }

    private void dadoParticipantesNaSessao() {
        SessaoMesa sessao = new SessaoMesa(SESSAO_ID);
        participantesDaSessao = List.of(
                new ParticipanteSessao(sessao, TokenUsuario.create(), "Maria"),
                new ParticipanteSessao(sessao, TokenUsuario.create(), "João")
        );

        when(participanteSessaoRepository.listarParticipantes(SESSAO_ID))
                .thenReturn(participantesDaSessao);
    }

    private void quandoListar() {
        response = useCase.execute(new ListarParticipantesCommand(SESSAO_ID, tokenConsultante));
    }

    private void quandoListarComErro() {
        try {
            useCase.execute(new ListarParticipantesCommand(SESSAO_ID, tokenConsultante));
        } catch (Exception e) {
            exception = e;
        }
    }

    private void deveRetornarParticipantesDaSessao() {
        assertEquals(participantesDaSessao, response);
    }

    private void deveRejeitarConsulta() {
        assertInstanceOf(ParticipanteSessaoNaoEncontradoException.class, exception);
        assertEquals("Participante não encontrado, token identificador inválido", exception.getMessage());

        verify(participanteSessaoRepository, never()).listarParticipantes(anyInt());
    }
}
