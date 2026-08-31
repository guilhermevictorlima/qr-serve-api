package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.sessao.SessaoMesaFactory;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.presentation.sessao.form.AcessarSessaoMesaForm;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponse;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponseStatus;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoMesaServiceTest {

    private static final Integer MESA_ID = 1;
    private static final Integer SESSAO_ID = 10;
    private static final String NOME_USUARIO = "Guilherme";

    private static final LocalDateTime DATA_HORA_ATUAL = LocalDateTime.of(2026, 8, 28, 14, 23, 0);

    @Mock
    private SessaoMesaFactory sessaoMesaFactory;

    @Mock
    private ParticipanteSessaoRepository participanteSessaoRepository;

    @Mock
    private SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;

    @Mock
    private TimeProvider timeProvider;

    private SessaoMesaService service;

    private AcessarSessaoMesaForm form;
    private SessaoMesa sessao;
    private AcessarSessaoMesaResponse response;

    @BeforeEach
    void setUp() {
        service = new SessaoMesaService(
                sessaoMesaFactory,
                participanteSessaoRepository,
                solicitacaoEntradaSessaoRepository,
                timeProvider
        );
    }

    @Test
    void dadoSessaoDentroDoTempoLimiteQuandoUsuarioTentarAcessarDeveLiberarAcesso() {
        dadoHorarioAtual();
        dadoUmaSessaoComInicioHaDoisMinutos();
        dadoUmFormularioDeAcesso();
        dadoQueAFactoryRetornaASessao();

        quandoUsuarioTentarAcessar();

        deveRetornarAcessoLiberado();
        deveCriarUmParticipanteNaSessao();
        naoDeveCriarUmaSolicitacaoDeEntrada();
    }

    @Test
    void dadoSessaoExatamenteNoLimiteDeTresMinutosQuandoUsuarioTentarAcessarDeveLiberarAcesso() {
        dadoHorarioAtual();
        dadoUmaSessaoComInicioHaTresMinutos();
        dadoUmFormularioDeAcesso();
        dadoQueAFactoryRetornaASessao();

        quandoUsuarioTentarAcessar();

        deveRetornarAcessoLiberado();
        deveCriarUmParticipanteNaSessao();
        naoDeveCriarUmaSolicitacaoDeEntrada();
    }

    @Test
    void dadoSessaoComTempoLimiteExpiradoQuandoUsuarioTentarAcessarDeveSolicitarPermissao() {
        dadoHorarioAtual();
        dadoUmaSessaoComInicioHaQuatroMinutos();
        dadoUmFormularioDeAcesso();
        dadoQueAFactoryRetornaASessao();

        quandoUsuarioTentarAcessar();

        deveRetornarPermissaoRequerida();
        deveCriarUmaSolicitacaoDeEntrada();
        naoDeveCriarUmParticipanteNaSessao();
    }

    @Test
    void dadoSessaoInexistenteQuandoUsuarioTentarAcessarDeveLancarExcecao() {
        dadoUmFormularioDeAcesso();
        dadoQueAFactoryLancaUmaExcecao();

        quandoUsuarioTentarAcessarEsperandoUmaExcecao();

        naoDeveCriarUmParticipanteNaSessao();
        naoDeveCriarUmaSolicitacaoDeEntrada();
    }

    private void dadoHorarioAtual() {
        when(timeProvider.dataHoraAtual()).thenReturn(DATA_HORA_ATUAL);
    }

    private void dadoUmaSessaoComInicioHaDoisMinutos() {
        sessao = criarSessao(DATA_HORA_ATUAL.minusMinutes(2));
    }

    private void dadoUmaSessaoComInicioHaTresMinutos() {
        sessao = criarSessao(DATA_HORA_ATUAL.minusMinutes(3));
    }

    private void dadoUmaSessaoComInicioHaQuatroMinutos() {
        sessao = criarSessao(DATA_HORA_ATUAL.minusMinutes(4));
    }

    private SessaoMesa criarSessao(LocalDateTime dataHoraInicio) {
        SessaoMesa sessao = new SessaoMesa();
        sessao.setId(SESSAO_ID);
        sessao.setDataHoraInicio(dataHoraInicio);
        return sessao;
    }

    private void dadoUmFormularioDeAcesso() {
        form = new AcessarSessaoMesaForm(
                MESA_ID,
                NOME_USUARIO
        );
    }

    private void dadoQueAFactoryRetornaASessao() {
        when(sessaoMesaFactory.obter(MESA_ID))
                .thenReturn(sessao);
    }

    private void dadoQueAFactoryLancaUmaExcecao() {
        when(sessaoMesaFactory.obter(MESA_ID))
                .thenThrow(new IllegalArgumentException("Sessão não encontrada"));
    }

    private void quandoUsuarioTentarAcessar() {
        response = service.acessar(form);
    }

    private void quandoUsuarioTentarAcessarEsperandoUmaExcecao() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.acessar(form)
        );

        assertEquals(
                "Sessão não encontrada",
                exception.getMessage()
        );
    }

    private void deveRetornarAcessoLiberado() {
        assertNotNull(response);

        assertEquals(
                SESSAO_ID,
                response.sessaoMesaId()
        );

        assertEquals(
                AcessarSessaoMesaResponseStatus.ACESSO_LIBERADO,
                response.responseStatus()
        );

        deveGerarUmTokenDeUsuarioValido();
    }

    private void deveRetornarPermissaoRequerida() {
        assertNotNull(response);

        assertEquals(
                SESSAO_ID,
                response.sessaoMesaId()
        );

        assertEquals(
                AcessarSessaoMesaResponseStatus.PERMISSAO_REQUERIDA,
                response.responseStatus()
        );

        deveGerarUmTokenDeUsuarioValido();
    }

    private void deveGerarUmTokenDeUsuarioValido() {
        assertNotNull(response.tokenUsuario());
        assertTrue(response.tokenUsuario().isBlank() == false);

        UUID token = UUID.fromString(response.tokenUsuario());

        assertNotNull(token);
    }

    private void deveCriarUmParticipanteNaSessao() {
        ArgumentCaptor<ParticipanteSessao> captor =
                ArgumentCaptor.forClass(ParticipanteSessao.class);

        verify(participanteSessaoRepository)
                .save(captor.capture());

        ParticipanteSessao participante = captor.getValue();

        assertEquals(
                sessao,
                participante.getSessao()
        );

        assertEquals(
                NOME_USUARIO,
                participante.getNome()
        );

        assertEquals(
                response.tokenUsuario(),
                participante.getToken()
        );
    }

    private void naoDeveCriarUmParticipanteNaSessao() {
        verify(participanteSessaoRepository, never())
                .save(any(ParticipanteSessao.class));
    }

    private void deveCriarUmaSolicitacaoDeEntrada() {
        ArgumentCaptor<SolicitacaoEntradaSessao> captor =
                ArgumentCaptor.forClass(SolicitacaoEntradaSessao.class);

        verify(solicitacaoEntradaSessaoRepository)
                .save(captor.capture());

        SolicitacaoEntradaSessao solicitacao = captor.getValue();

        assertEquals(
                sessao,
                solicitacao.getSessao()
        );

        assertEquals(
                NOME_USUARIO,
                solicitacao.getNome()
        );

        assertEquals(
                response.tokenUsuario(),
                solicitacao.getToken()
        );

        assertNotNull(solicitacao.getDataHoraSolicitacao());
    }

    private void naoDeveCriarUmaSolicitacaoDeEntrada() {
        verify(solicitacaoEntradaSessaoRepository, never())
                .save(any(SolicitacaoEntradaSessao.class));
    }
}