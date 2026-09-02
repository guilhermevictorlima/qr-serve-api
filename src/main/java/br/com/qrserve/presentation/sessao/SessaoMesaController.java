package br.com.qrserve.presentation.sessao;

import br.com.qrserve.application.sessao.AcessarSessaoUseCase;
import br.com.qrserve.application.sessao.ResponderSolicitacaoEntradaSessaoUseCase;
import br.com.qrserve.presentation.sessao.form.AcessarSessaoMesaForm;
import br.com.qrserve.presentation.sessao.form.ResponderSolicitacaoEntradaSessaoForm;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessao")
public class SessaoMesaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessaoMesaController.class);

    private final AcessarSessaoUseCase acessarSessaoUseCase;
    private final ResponderSolicitacaoEntradaSessaoUseCase responderSolicitacaoEntradaSessaoUseCase;

    public SessaoMesaController(AcessarSessaoUseCase acessarSessaoUseCase, ResponderSolicitacaoEntradaSessaoUseCase responderSolicitacaoEntradaSessaoUseCase) {
        this.acessarSessaoUseCase = acessarSessaoUseCase;
        this.responderSolicitacaoEntradaSessaoUseCase = responderSolicitacaoEntradaSessaoUseCase;
    }

    @PostMapping("/acessar")
    public AcessarSessaoMesaResponse acessar(@Valid @RequestBody AcessarSessaoMesaForm form) {
        LOGGER.info("[POST]/sessao/acessar: {}", form.toString());
        return acessarSessaoUseCase.execute(form);
    }

    @PatchMapping("/solicitacao")
    public void responderSolicitacaoEntradaSessao(@Valid @RequestBody ResponderSolicitacaoEntradaSessaoForm form) {
        LOGGER.info("[PUT]/sessao/solicitacao: {}", form.toString());
        responderSolicitacaoEntradaSessaoUseCase.execute(form);
    }

}
