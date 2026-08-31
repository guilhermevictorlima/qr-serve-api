package br.com.qrserve.presentation.participantesessao.response;

import br.com.qrserve.domain.sessao.ParticipanteSessao;

public record ParticipanteSessaoResponse(
    Integer sessaoMesaId,
    String token,
    String nome
) {
    public static ParticipanteSessaoResponse from(ParticipanteSessao participanteSessao) {
        return new ParticipanteSessaoResponse(participanteSessao.getSessao().getId(), participanteSessao.getToken().getValor(), participanteSessao.getNome());
    }
}
