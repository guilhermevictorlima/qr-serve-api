package br.com.qrserve.presentation.sessao;

import br.com.qrserve.presentation.sessao.form.AcessarSessaoMesaForm;
import br.com.qrserve.presentation.sessao.form.ResponderSolicitacaoEntradaSessaoForm;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponse;
import br.com.qrserve.application.sessao.SessaoMesaService;
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

    private final SessaoMesaService service;

    public SessaoMesaController(SessaoMesaService service) {
        this.service = service;
    }

    @PostMapping("/acessar")
    public AcessarSessaoMesaResponse acessar(@Valid @RequestBody AcessarSessaoMesaForm form) {
        LOGGER.info("[POST]/sessao/acessar: {}", form.toString());
        return service.acessar(form);
    }

    @PatchMapping("/solicitacao")
    public void responderSolicitacaoEntradaSessao(@Valid @RequestBody ResponderSolicitacaoEntradaSessaoForm form) {
        LOGGER.info("[PUT]/sessao/solicitacao: {}", form.toString());
        service.responderSolicitacaoEntradaSessao(form);
    }

}
