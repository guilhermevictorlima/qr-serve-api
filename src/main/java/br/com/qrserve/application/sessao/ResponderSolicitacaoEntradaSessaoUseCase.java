package br.com.qrserve.application.sessao;

import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import br.com.qrserve.presentation.sessao.form.ResponderSolicitacaoEntradaSessaoForm;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public void execute(ResponderSolicitacaoEntradaSessaoForm form) {
        SolicitacaoEntradaSessao solicitacao = obterSolicitacao(form);
        ParticipanteSessao participanteRespondente = obterParticipanteRespondente(form); // TODO será usado na implementação da notificação via web socket, na task QR-004

        if (form.resposta().isSolicitacaoAprovada()) {
            participanteSessaoRepository.save(ParticipanteSessao.from(solicitacao));
        } else {
            feedbackSender.notificar();
        }

        solicitacaoEntradaSessaoRepository.delete(solicitacao);
    }

    private SolicitacaoEntradaSessao obterSolicitacao(ResponderSolicitacaoEntradaSessaoForm form) {
        return solicitacaoEntradaSessaoRepository
                .obterSolicitacaoPorToken(form.tokenUsuarioSolicitante())
                .orElseThrow(() -> {
                    LOGGER.error("SOLICITAÇÃO NÃO ENCONTRADA -- {}", form);
                    return new IllegalArgumentException("Solicitação de entrada não encontrada. Verifique se já foi respondida e tente novamente");
                });
    }

    private ParticipanteSessao obterParticipanteRespondente(ResponderSolicitacaoEntradaSessaoForm form) {
        return participanteSessaoRepository.obterParticipantePorToken(form.tokenUsuarioRespondente())
                .orElseThrow(() -> {
                    LOGGER.error("PARTICIPANTE NÃO ENCONTRADO -- {}", form);
                    return new IllegalArgumentException("Participante respondente não encontrado, token identificador inválido");
                });
    }

}
