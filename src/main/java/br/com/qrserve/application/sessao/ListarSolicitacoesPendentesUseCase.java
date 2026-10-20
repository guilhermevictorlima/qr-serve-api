package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ListarSolicitacoesPendentesUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListarSolicitacoesPendentesUseCase.class);

    private final SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;
    private final ParticipanteSessaoRepository participanteSessaoRepository;
    private final TimeProvider timeProvider;

    public ListarSolicitacoesPendentesUseCase(SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository, ParticipanteSessaoRepository participanteSessaoRepository, TimeProvider timeProvider) {
        this.solicitacaoEntradaSessaoRepository = solicitacaoEntradaSessaoRepository;
        this.participanteSessaoRepository = participanteSessaoRepository;
        this.timeProvider = timeProvider;
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoEntradaSessao> execute(ListarSolicitacoesPendentesCommand command) {
        validarConsultantePertenceASessao(command);

        LocalDateTime dataHoraAtual = timeProvider.dataHoraAtual();

        return solicitacaoEntradaSessaoRepository.listarPorSessao(command.sessaoId())
                .stream()
                .filter(solicitacao -> !solicitacao.isExpirada(dataHoraAtual))
                .toList();
    }

    private void validarConsultantePertenceASessao(ListarSolicitacoesPendentesCommand command) {
        ParticipanteSessao consultante = participanteSessaoRepository.obterParticipantePorToken(command.tokenUsuarioConsultante())
                .orElseThrow(() -> {
                    LOGGER.error("PARTICIPANTE NÃO ENCONTRADO -- {}", command);
                    return new ParticipanteSessaoNaoEncontradoException("Participante não encontrado, token identificador inválido");
                });

        if (!Objects.equals(consultante.getSessao().getId(), command.sessaoId())) {
            LOGGER.error("CONSULTANTE DE OUTRA SESSÃO -- sessaoId: {} / consultanteId: {}", command.sessaoId(), consultante.getId());
            throw new ParticipanteSessaoNaoEncontradoException("Participante não encontrado, token identificador inválido");
        }
    }
}
