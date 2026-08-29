package br.com.qrserve.models.dto.response;

import br.com.qrserve.models.data.Mesa;
import br.com.qrserve.models.data.ParticipanteSessao;

public record ParticipanteSessaoResponse(
    Integer sessaoMesaId,
    String token,
    String nome
) {
    public static ParticipanteSessaoResponse from(ParticipanteSessao participanteSessao) {
        return new ParticipanteSessaoResponse(participanteSessao.getSessao().getId(), participanteSessao.getToken(), participanteSessao.getNome());
    }
}
