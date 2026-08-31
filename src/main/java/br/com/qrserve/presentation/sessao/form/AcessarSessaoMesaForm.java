package br.com.qrserve.presentation.sessao.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcessarSessaoMesaForm(
    @NotNull(message = "O identificador da mesa é obrigatório para acessar a sessão") Integer mesaId,
    @NotBlank(message = "O nome é obrigatório para acessar a sessão") String nome
) {}
