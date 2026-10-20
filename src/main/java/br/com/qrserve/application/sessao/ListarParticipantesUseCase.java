package br.com.qrserve.application.sessao;

import br.com.qrserve.domain.exception.ParticipanteSessaoNaoEncontradoException;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ListarParticipantesUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListarParticipantesUseCase.class);

    private final ParticipanteSessaoRepository participanteSessaoRepository;

    public ListarParticipantesUseCase(ParticipanteSessaoRepository participanteSessaoRepository) {
        this.participanteSessaoRepository = participanteSessaoRepository;
    }

    @Transactional(readOnly = true)
    public List<ParticipanteSessao> execute(ListarParticipantesCommand command) {
        validarConsultantePertenceASessao(command);

        return participanteSessaoRepository.listarParticipantes(command.sessaoId());
    }

    private void validarConsultantePertenceASessao(ListarParticipantesCommand command) {
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
