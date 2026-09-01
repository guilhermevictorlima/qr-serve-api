package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SessaoMesaRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import br.com.qrserve.presentation.sessao.form.AcessarSessaoMesaForm;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponse;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcessarSessaoUseCaseTest {
    private static final LocalDateTime DATA_HORA_ATUAL = LocalDateTime.of(2026, 9, 1, 12, 34, 50);

    @Mock
    private SessaoMesaRepository repository;

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;

    @Mock
    private ParticipanteSessaoRepository participanteSessaoRepository;

    @Captor
    private ArgumentCaptor<SessaoMesa> sessaoMesaArgumentCaptor;

    @InjectMocks
    private AcessarSessaoUseCase useCase;

    private AcessarSessaoMesaForm form;
    private SessaoMesa sessao;
    private AcessarSessaoMesaResponse response;

    @BeforeEach
    void setUp() {
        form = new AcessarSessaoMesaForm(
                1,
                "Guilherme"
        );

        when(timeProvider.dataHoraAtual()).thenReturn(DATA_HORA_ATUAL);
    }

    @Test
    @DisplayName("Quando não existir sessão na mesa, criar uma nova e acessar")
    void cenario_1() {
        dadoSessaoInexistente();
        quandoAcessar();
        deveCriarSessao();
        deveRegistrarParticipante();
        deveRetornarAcessoLiberado();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    @DisplayName("Quando existir sessão ativa e for permitido entrar, acessar a sessão")
    void cenario_2(int tempoMinutos) {
        dadoSessaoAtiva();
        dadoEntradaPermitida(tempoMinutos);
        quandoAcessar();
        deveRegistrarParticipante();
        deveRetornarAcessoLiberado();
    }

    @Test
    @DisplayName("Quando existir sessão ativa e não for permitido entrar, solicitar permissão")
    void cenario_3() {
        dadoSessaoAtiva();
        dadoEntradaNaoPermitida();
        quandoAcessar();
        deveRegistrarSolicitacaoEntrada();
        deveRetornarPermissaoRequerida();
    }

    private void dadoSessaoInexistente() {
        when(repository.obterSessaoAtiva(form.mesaId()))
                .thenReturn(Optional.empty());

        when(repository.save(any(SessaoMesa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void dadoSessaoAtiva() {
        sessao = SessaoMesa.create(
                form.mesaId(),
                DATA_HORA_ATUAL
        );

        when(repository.obterSessaoAtiva(form.mesaId()))
                .thenReturn(Optional.of(sessao));
    }

    private void dadoEntradaPermitida(int tempoMinutos) {
        sessao.setDataHoraInicio(DATA_HORA_ATUAL.minusMinutes(tempoMinutos));
    }

    private void dadoEntradaNaoPermitida() {
        sessao.setDataHoraInicio(DATA_HORA_ATUAL.minusMinutes(4));
    }

    private void quandoAcessar() {
        response = useCase.execute(form);
    }

    private void deveCriarSessao() {
        verify(repository).save(sessaoMesaArgumentCaptor.capture());
        sessao = sessaoMesaArgumentCaptor.getValue();
    }

    private void deveRegistrarParticipante() {
        verify(participanteSessaoRepository)
                .save(any(ParticipanteSessao.class));

        verify(solicitacaoEntradaSessaoRepository, never())
                .save(any(SolicitacaoEntradaSessao.class));
    }

    private void deveRetornarAcessoLiberado() {
        assertEquals(AcessarSessaoMesaResponseStatus.ACESSO_LIBERADO, response.responseStatus());
    }

    private void deveRegistrarSolicitacaoEntrada() {
        verify(solicitacaoEntradaSessaoRepository)
                .save(any(SolicitacaoEntradaSessao.class));

        verify(participanteSessaoRepository, never())
                .save(any(ParticipanteSessao.class));
    }

    private void deveRetornarPermissaoRequerida() {
        assertEquals(AcessarSessaoMesaResponseStatus.PERMISSAO_REQUERIDA, response.responseStatus());
    }

}