package br.com.qrserve.application.sessao;

public record AcessarSessaoCommand(
        Integer mesaId,
        String nome
) {
}
