package br.com.qrserve.application.sessao;

public record ListarSolicitacoesPendentesCommand(
        Integer sessaoId,
        String tokenUsuarioConsultante
) {

    @Override
    public String toString() {
        return "ListarSolicitacoesPendentesCommand[sessaoId=" + sessaoId + "]";
    }
}
