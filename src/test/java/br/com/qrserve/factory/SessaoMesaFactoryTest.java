package br.com.qrserve.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.qrserve.models.data.Mesa;
import br.com.qrserve.models.data.SessaoMesa;
import br.com.qrserve.repositories.SessaoMesaRepository;

@ExtendWith(MockitoExtension.class)
class SessaoMesaFactoryTest {

    private static final int TAMANHO_TOKEN_SESSAO = 10;
    private static final DateTimeFormatter FORMATO_DATA_TOKEN = DateTimeFormatter.ofPattern("ddMMyy");

    @Mock
    private SessaoMesaRepository sessaoRepository;

    @InjectMocks
    private SessaoMesaFactory sessaoMesaFactory;

    private Integer mesaId;
    private SessaoMesa sessaoAtivaExistente;
    private SessaoMesa sessaoRetornadaPeloSave;
    private SessaoMesa resultado;
    private ArgumentCaptor<SessaoMesa> sessaoCaptor;
    private LocalDateTime instanteAntesDaChamada;
    private LocalDateTime instanteDepoisDaChamada;

    @BeforeEach
    void setUp() {
        mesaId = 7;
        sessaoCaptor = ArgumentCaptor.forClass(SessaoMesa.class);
    }

    @Test
    void quandoHouverSessaoAtivaNaoDeveCriarNova() {
        dadoQueMesaPossuiSessaoAtiva();
        quandoObterSessaoDaMesa();
        deveBuscarPorSessaoAtiva();
        deveRetornarASessaoExistente();
        deveNaoSalvarNovaSessao();
    }

    @Test
    void dadoMesaSemSessaoAtiva_quandoObter_deveCriarNovaSessaoComTokenEDadosCorretos() {
        dadoQueMesaNaoPossuiSessaoAtiva();
        dadoQueRepositorioSalvaSessaoComSucesso();

        quandoObterSessaoDaMesa();

        deveBuscarPorSessaoAtiva();
        deveSalvarExatamenteUmaNovaSessao();
        deveAssociarSessaoCriadaAMesaCorreta();
        deveGerarTokenComTamanhoEsperado();
        deveGerarTokenComSufixoDeDataCorreto();
        deveDefinirDataHoraInicioProximaDoMomentoDaChamada();
        deveRetornarASessaoCriadaPeloRepositorio();
    }

    @Test
    void dadoQualquerCenario_quandoObter_deveConsultarSessaoAtivaComMesaIdRecebido() {
        dadoQueMesaPossuiSessaoAtiva();

        quandoObterSessaoDaMesa();

        verify(sessaoRepository, times(1)).obterSessaoAtiva(eq(mesaId));
    }

    @Test
    void dadoMesaIdNulo_quandoObter_deveConsultarRepositorioComNuloSemLancarExcecaoPropria() {
        mesaId = null;
        when(sessaoRepository.obterSessaoAtiva(null)).thenReturn(Optional.empty());
        dadoQueRepositorioSalvaSessaoComSucesso();

        quandoObterSessaoDaMesa();

        verify(sessaoRepository, times(1)).obterSessaoAtiva(null);
        verify(sessaoRepository, times(1)).save(any(SessaoMesa.class));
    }

    @Test
    void dadoErroAoSalvarNovaSessao_quandoObter_devePropagarExcecao() {
        dadoQueMesaNaoPossuiSessaoAtiva();
        dadoQueRepositorioLancaErroAoSalvar();

        assertThrows(RuntimeException.class, this::quandoObterSessaoDaMesa);

        deveTerTentadoSalvarUmaVez();
    }

    private void dadoQueMesaPossuiSessaoAtiva() {
        sessaoAtivaExistente = new SessaoMesa(new Mesa(mesaId), "token-existente-abc", LocalDateTime.now().minusMinutes(10));
        when(sessaoRepository.obterSessaoAtiva(mesaId)).thenReturn(Optional.of(sessaoAtivaExistente));
    }

    private void deveRetornarASessaoExistente() {
        assertThat(resultado).isSameAs(sessaoAtivaExistente);
    }

    private void deveNaoSalvarNovaSessao() {
        verify(sessaoRepository, never()).save(any(SessaoMesa.class));
    }

    private void dadoQueMesaNaoPossuiSessaoAtiva() {
        when(sessaoRepository.obterSessaoAtiva(mesaId)).thenReturn(Optional.empty());
    }

    private void dadoQueRepositorioSalvaSessaoComSucesso() {
        sessaoRetornadaPeloSave = new SessaoMesa(new Mesa(mesaId), "token-persistido", LocalDateTime.now());
        when(sessaoRepository.save(any(SessaoMesa.class))).thenReturn(sessaoRetornadaPeloSave);
    }

    private void deveSalvarExatamenteUmaNovaSessao() {
        verify(sessaoRepository, times(1)).save(sessaoCaptor.capture());
    }

    private void deveAssociarSessaoCriadaAMesaCorreta() {
        SessaoMesa sessaoCriada = sessaoCaptor.getValue();
        assertThat(sessaoCriada.getMesa()).isNotNull();
        assertThat(sessaoCriada.getMesa().getId()).isEqualTo(mesaId);
    }

    private void deveGerarTokenComTamanhoEsperado() {
        String token = sessaoCaptor.getValue().getToken();
        int tamanhoEsperado = TAMANHO_TOKEN_SESSAO + String.valueOf(mesaId).length() + 6;

        assertThat(token).isNotBlank();
        assertThat(token).hasSize(tamanhoEsperado);
    }

    private void deveGerarTokenComSufixoDeDataCorreto() {
        SessaoMesa sessaoCriada = sessaoCaptor.getValue();
        String sufixoEsperado = sessaoCriada.getDataHoraInicio().format(FORMATO_DATA_TOKEN);

        assertThat(sessaoCriada.getToken()).endsWith(sufixoEsperado);
    }

    private void deveDefinirDataHoraInicioProximaDoMomentoDaChamada() {
        LocalDateTime dataHoraInicio = sessaoCaptor.getValue().getDataHoraInicio();

        assertThat(dataHoraInicio).isNotNull();
        assertThat(dataHoraInicio)
                .isAfterOrEqualTo(instanteAntesDaChamada)
                .isBeforeOrEqualTo(instanteDepoisDaChamada);
    }

    private void deveRetornarASessaoCriadaPeloRepositorio() {
        assertThat(resultado).isSameAs(sessaoRetornadaPeloSave);
    }

    private void dadoQueRepositorioLancaErroAoSalvar() {
        when(sessaoRepository.save(any(SessaoMesa.class)))
                .thenThrow(new RuntimeException("Falha simulada ao persistir sessão"));
    }

    private void deveTerTentadoSalvarUmaVez() {
        verify(sessaoRepository, times(1)).save(any(SessaoMesa.class));
    }

    private void quandoObterSessaoDaMesa() {
        instanteAntesDaChamada = LocalDateTime.now();
        resultado = sessaoMesaFactory.obter(mesaId);
        instanteDepoisDaChamada = LocalDateTime.now();
    }

    private void deveBuscarPorSessaoAtiva() {
        verify(sessaoRepository, times(1)).obterSessaoAtiva(mesaId);
    }
}
