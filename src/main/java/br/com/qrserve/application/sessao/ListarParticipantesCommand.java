package br.com.qrserve.application.sessao;

public record ListarParticipantesCommand(
        Integer sessaoId,
        String tokenUsuarioConsultante
) {

    @Override
    public String toString() {
        return "ListarParticipantesCommand[sessaoId=" + sessaoId + "]";
    }
}
