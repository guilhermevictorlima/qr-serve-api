package br.com.qrserve.application.sessao;

public record AcessarSessaoResult(
        String tokenUsuario,
        Integer sessaoMesaId,
        StatusAcessoSessao status
) {
}
