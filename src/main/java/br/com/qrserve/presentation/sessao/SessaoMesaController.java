package br.com.qrserve.presentation.sessao;

import br.com.qrserve.application.sessao.AcessarSessaoUseCase;
import br.com.qrserve.application.sessao.ListarSolicitacoesPendentesCommand;
import br.com.qrserve.application.sessao.ListarSolicitacoesPendentesUseCase;
import br.com.qrserve.application.sessao.ResponderSolicitacaoEntradaSessaoUseCase;
import br.com.qrserve.presentation.sessao.form.AcessarSessaoMesaForm;
import br.com.qrserve.presentation.sessao.form.ResponderSolicitacaoEntradaSessaoForm;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponse;
import br.com.qrserve.presentation.sessao.response.SolicitacaoEntradaSessaoResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sessao")
public class SessaoMesaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessaoMesaController.class);

    private final AcessarSessaoUseCase acessarSessaoUseCase;
    private final ResponderSolicitacaoEntradaSessaoUseCase responderSolicitacaoEntradaSessaoUseCase;
    private final ListarSolicitacoesPendentesUseCase listarSolicitacoesPendentesUseCase;

    public SessaoMesaController(AcessarSessaoUseCase acessarSessaoUseCase, ResponderSolicitacaoEntradaSessaoUseCase responderSolicitacaoEntradaSessaoUseCase, ListarSolicitacoesPendentesUseCase listarSolicitacoesPendentesUseCase) {
        this.listarSolicitacoesPendentesUseCase = listarSolicitacoesPendentesUseCase;
        this.acessarSessaoUseCase = acessarSessaoUseCase;
        this.responderSolicitacaoEntradaSessaoUseCase = responderSolicitacaoEntradaSessaoUseCase;
    }

    @PostMapping("/acessar")
    public AcessarSessaoMesaResponse acessar(@Valid @RequestBody AcessarSessaoMesaForm form) {
        LOGGER.info("[POST]/sessao/acessar: {}", form.toString());
        return AcessarSessaoMesaResponse.from(acessarSessaoUseCase.execute(form.toCommand()));
    }

    @GetMapping("/{sessaoId}/solicitacoes")
    public List<SolicitacaoEntradaSessaoResponse> listarSolicitacoes(
            @PathVariable(name = "sessaoId") Integer sessaoId,
            @RequestHeader(name = "X-Token-Usuario") String tokenUsuario
    ) {
        LOGGER.info("[GET]/sessao/{}/solicitacoes", sessaoId);
        return listarSolicitacoesPendentesUseCase.execute(new ListarSolicitacoesPendentesCommand(sessaoId, tokenUsuario))
                .stream()
                .map(SolicitacaoEntradaSessaoResponse::from)
                .toList();
    }

    @PatchMapping("/solicitacao")
    public void responderSolicitacaoEntradaSessao(@Valid @RequestBody ResponderSolicitacaoEntradaSessaoForm form) {
        LOGGER.info("[PATCH]/sessao/solicitacao: {}", form.toString());
        responderSolicitacaoEntradaSessaoUseCase.execute(form.toCommand());
    }

}
