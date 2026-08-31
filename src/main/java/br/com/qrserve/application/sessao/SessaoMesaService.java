package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.sessao.SessaoMesaFactory;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.domain.sessao.TokenUsuario;
import br.com.qrserve.presentation.sessao.form.AcessarSessaoMesaForm;
import br.com.qrserve.presentation.sessao.form.ResponderSolicitacaoEntradaSessaoForm;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponse;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponseStatus;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.text.MessageFormat.format;

@Service
public class SessaoMesaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessaoMesaService.class);

    private static final int TEMPO_LIMITE_EM_MINUTOS_SESSAO_ABERTA = 3;

    private final SessaoMesaFactory sessaoMesaFactory;
    private final ParticipanteSessaoRepository participanteSessaoRepository;
    private final SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;
    private final TimeProvider timeProvider;

    public SessaoMesaService(SessaoMesaFactory sessaoMesaFactory, ParticipanteSessaoRepository participanteSessaoRepository, SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository, TimeProvider timeProvider) {
        this.sessaoMesaFactory = sessaoMesaFactory;
        this.participanteSessaoRepository = participanteSessaoRepository;
        this.solicitacaoEntradaSessaoRepository = solicitacaoEntradaSessaoRepository;
        this.timeProvider = timeProvider;
    }

    @Transactional(rollbackOn = Exception.class)
    public AcessarSessaoMesaResponse acessar(AcessarSessaoMesaForm form) {
        TokenUsuario tokenUsuario = TokenUsuario.create();

        SessaoMesa sessao = sessaoMesaFactory.obter(form.mesaId());
        AcessarSessaoMesaResponseStatus responseStatus = tentarEntrarNaSessao(sessao, form, tokenUsuario);

        return new AcessarSessaoMesaResponse(tokenUsuario.getValor(), sessao.getId(), responseStatus);
    }

    // TODO implementar feedback em websocket ao solicitante e aos participantes da mesa
    @Transactional(rollbackOn = Exception.class)
    public void responderSolicitacaoEntradaSessao(ResponderSolicitacaoEntradaSessaoForm form) {
        SolicitacaoEntradaSessao solicitacao = solicitacaoEntradaSessaoRepository
                .obterSolicitacaoPorToken(form.tokenUsuarioSolicitante())
                .orElseThrow(() -> new IllegalArgumentException(
                        format("Não foi encontrada uma solicitação de entrada feita por um usuário com este token -- tokenUsuarioSolicitante: {0}", form.tokenUsuarioSolicitante())
                ));

        ParticipanteSessao participanteRespondente = participanteSessaoRepository.obterParticipantePorToken(form.tokenUsuarioRespondente())
                .orElseThrow(() -> new IllegalArgumentException(
                        format("Não foi encontrado um participante de sessão com esse token -- token: {0}", form.tokenUsuarioRespondente())
                ));

        if (form.resposta().isSolicitacaoAprovada()) {
            participanteSessaoRepository.save(
                    new ParticipanteSessao(
                            solicitacao.getSessao(),
                            solicitacao.getToken(),
                            solicitacao.getNome()
                    )
            );

            LOGGER.info(
                    "SOLICITAÇÃO DE ENTRADA APROVADA -- solicitaçãoId: {} / idRespondente: {}",
                    solicitacao.getId(),
                    participanteRespondente.getId()
            );
        } else {
            LOGGER.info(
                    "SOLICITAÇÃO DE ENTRADA NEGADA -- solicitaçãoId: {} / idRespondente: {}",
                    solicitacao.getId(),
                    participanteRespondente.getId()
            );
        }

        solicitacaoEntradaSessaoRepository.delete(solicitacao);
    }

    private AcessarSessaoMesaResponseStatus tentarEntrarNaSessao(SessaoMesa sessaoMesa, AcessarSessaoMesaForm form, TokenUsuario tokenUsuario) {
        LocalDateTime now = timeProvider.dataHoraAtual();
        boolean jaPassouTempoLimiteDeSessaoAberta = now.isAfter(sessaoMesa.getDataHoraInicio().plusMinutes(TEMPO_LIMITE_EM_MINUTOS_SESSAO_ABERTA));
        if (jaPassouTempoLimiteDeSessaoAberta) {
            LOGGER.warn("TEMPO DE ENTRADA LIVRE NA SESSÃO EXPIRADO -- {} / tokenUsuario: {}", form, tokenUsuario);
            solicitacaoEntradaSessaoRepository.save(new SolicitacaoEntradaSessao(sessaoMesa, tokenUsuario, form.nome(), now));
            return AcessarSessaoMesaResponseStatus.PERMISSAO_REQUERIDA;
        } else {
            participanteSessaoRepository.save(new ParticipanteSessao(sessaoMesa, tokenUsuario, form.nome()));
            return AcessarSessaoMesaResponseStatus.ACESSO_LIBERADO;
        }

    }
}
