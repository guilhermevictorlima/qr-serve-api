package br.com.qrserve.application.sessao;

import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.exception.SolicitacaoEntradaSessaoNaoEncontradaException;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ResponderSolicitacaoEntradaSessaoUseCase {
    private final Logger LOGGER = LoggerFactory.getLogger(ResponderSolicitacaoEntradaSessaoUseCase.class);

    private final SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;
    private final ParticipanteSessaoRepository participanteSessaoRepository;
    private final FeedbackSender feedbackSender;

    public ResponderSolicitacaoEntradaSessaoUseCase(SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository, ParticipanteSessaoRepository participanteSessaoRepository, FeedbackSender feedbackSender) {
        this.solicitacaoEntradaSessaoRepository = solicitacaoEntradaSessaoRepository;
        this.participanteSessaoRepository = participanteSessaoRepository;
        this.feedbackSender = feedbackSender;
    }

    @Transactional(rollbackOn = Exception.class)
    public void execute(ResponderSolicitacaoEntradaSessaoCommand command) {
        SolicitacaoEntradaSessao solicitacao = obterSolicitacao(command);
        ParticipanteSessao participanteRespondente = obterParticipanteRespondente(command);
        validarRespondentePertenceASessao(solicitacao, participanteRespondente);

        if (command.resposta().isAprovada()) {
            participanteSessaoRepository.save(ParticipanteSessao.from(solicitacao));
        } else {
            feedbackSender.notificar();
        }

        solicitacaoEntradaSessaoRepository.delete(solicitacao);
    }

    private SolicitacaoEntradaSessao obterSolicitacao(ResponderSolicitacaoEntradaSessaoCommand command) {
        return solicitacaoEntradaSessaoRepository
                .obterSolicitacaoPorToken(command.tokenUsuarioSolicitante())
                .orElseThrow(() -> {
                    LOGGER.error("SOLICITAÇÃO NÃO ENCONTRADA -- {}", command);
                    return new SolicitacaoEntradaSessaoNaoEncontradaException("Solicitação de entrada não encontrada. Verifique se já foi respondida e tente novamente");
                });
    }

    private void validarRespondentePertenceASessao(SolicitacaoEntradaSessao solicitacao, ParticipanteSessao respondente) {
        if (!Objects.equals(solicitacao.getSessao().getId(), respondente.getSessao().getId())) {
            LOGGER.error("RESPONDENTE DE OUTRA SESSÃO -- solicitacaoId: {} / respondenteId: {}", solicitacao.getId(), respondente.getId());
            throw new ParticipanteSessaoNaoEncontradoException("Participante respondente não encontrado, token identificador inválido");
        }
    }

    private ParticipanteSessao obterParticipanteRespondente(ResponderSolicitacaoEntradaSessaoCommand command) {
        return participanteSessaoRepository.obterParticipantePorToken(command.tokenUsuarioRespondente())
                .orElseThrow(() -> {
                    LOGGER.error("PARTICIPANTE NÃO ENCONTRADO -- {}", command);
                    return new ParticipanteSessaoNaoEncontradoException("Participante respondente não encontrado, token identificador inválido");
                });
    }

}
