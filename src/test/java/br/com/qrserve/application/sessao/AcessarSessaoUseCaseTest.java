package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.MesaNaoEncontradaException;
import br.com.qrserve.domain.mesa.Mesa;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.infrastructure.persistence.mesa.MesaRepository;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SessaoMesaRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcessarSessaoUseCaseTest {
    private static final LocalDateTime DATA_HORA_ATUAL = LocalDateTime.of(2026, 9, 1, 12, 34, 50);

    @Mock
    private SessaoMesaRepository repository;

    @Mock
    private MesaRepository mesaRepository;

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

    private AcessarSessaoCommand command;
    private SessaoMesa sessao;
    private AcessarSessaoResult response;
    private Mesa mesa;
    private Exception exception;

    @BeforeEach
    void setUp() {
        command = new AcessarSessaoCommand(
                1,
                "Guilherme"
        );

        lenient().when(timeProvider.dataHoraAtual()).thenReturn(DATA_HORA_ATUAL);
    }

    @Test
    @DisplayName("@spec:AC-001 Quando não existir sessão na mesa, criar uma nova e acessar")
    void cenario_1() {
        dadoMesa();
        dadoSessaoInexistente();
        quandoAcessar();
        deveCriarSessao();
        deveRegistrarParticipante();
        deveRetornarAcessoLiberado();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    @DisplayName("@spec:AC-003 Quando existir sessão ativa e for permitido entrar, acessar a sessão")
    void cenario_2(int tempoMinutos) {
        dadoMesa();
        dadoSessaoAtiva();
        dadoEntradaPermitida(tempoMinutos);
        quandoAcessar();
        naoDeveCriarNovaSessao();
        deveRegistrarParticipante();
        deveRetornarAcessoLiberado();
    }

    @Test
    @DisplayName("@spec:AC-004 Quando existir sessão ativa e não for permitido entrar, solicitar permissão")
    void cenario_3() {
        dadoMesa();
        dadoSessaoAtiva();
        dadoEntradaNaoPermitida();
        quandoAcessar();
        naoDeveCriarNovaSessao();
        deveRegistrarSolicitacaoEntrada();
        deveRetornarPermissaoRequerida();
    }

    @Test
    @DisplayName("@spec:AC-002 Quando a mesa não existir, rejeitar o acesso")
    void cenario_4() {
        dadoMesaInexistente();
        quandoAcessarComErro();
        deveRejeitarAcessoPorMesaInexistente();
    }

    @Test
    @DisplayName("@spec:AC-011 Quando um solicitante acessar de novo, registrar uma nova solicitação")
    void cenario_5() {
        dadoMesa();
        dadoSessaoAtiva();
        dadoEntradaNaoPermitida();
        quandoAcessar();
        quandoAcessar();
        deveRegistrarNovaSolicitacaoACadaAcesso();
    }

    private void dadoMesa() {
        mesa = new Mesa(command.mesaId());

        when(mesaRepository.obterParaAtualizar(command.mesaId()))
                .thenReturn(Optional.of(mesa));
    }

    private void dadoMesaInexistente() {
        when(mesaRepository.obterParaAtualizar(command.mesaId()))
                .thenReturn(Optional.empty());
    }

    private void quandoAcessarComErro() {
        try {
            useCase.execute(command);
        } catch (Exception e) {
            exception = e;
        }
    }

    private void deveRejeitarAcessoPorMesaInexistente() {
        assertInstanceOf(MesaNaoEncontradaException.class, exception);
        assertEquals("Mesa não encontrada, verifique o QR Code e tente novamente", exception.getMessage());

        verify(repository, never()).save(any(SessaoMesa.class));
        verify(participanteSessaoRepository, never()).save(any(ParticipanteSessao.class));
        verify(solicitacaoEntradaSessaoRepository, never()).save(any(SolicitacaoEntradaSessao.class));
    }

    private void dadoSessaoInexistente() {
        when(repository.obterSessaoAtiva(mesa.getId()))
                .thenReturn(Optional.empty());

        when(repository.save(any(SessaoMesa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void dadoSessaoAtiva() {
        sessao = SessaoMesa.create(
                mesa,
                DATA_HORA_ATUAL
        );

        when(repository.obterSessaoAtiva(mesa.getId()))
                .thenReturn(Optional.of(sessao));
    }

    private void dadoEntradaPermitida(int tempoMinutos) {
        sessao.setDataHoraInicio(DATA_HORA_ATUAL.minusMinutes(tempoMinutos));
    }

    private void dadoEntradaNaoPermitida() {
        sessao.setDataHoraInicio(DATA_HORA_ATUAL.minusMinutes(4));
    }

    private void quandoAcessar() {
        response = useCase.execute(command);
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
        assertEquals(StatusAcessoSessao.ACESSO_LIBERADO, response.status());
    }

    private void deveRegistrarSolicitacaoEntrada() {
        verify(solicitacaoEntradaSessaoRepository)
                .save(any(SolicitacaoEntradaSessao.class));

        verify(participanteSessaoRepository, never())
                .save(any(ParticipanteSessao.class));
    }

    private void deveRetornarPermissaoRequerida() {
        assertEquals(StatusAcessoSessao.PERMISSAO_REQUERIDA, response.status());
    }

    private void deveRegistrarNovaSolicitacaoACadaAcesso() {
        verify(solicitacaoEntradaSessaoRepository, times(2))
                .save(any(SolicitacaoEntradaSessao.class));
    }

    private void naoDeveCriarNovaSessao() {
        verify(repository, never()).save(any(SessaoMesa.class));
    }

}