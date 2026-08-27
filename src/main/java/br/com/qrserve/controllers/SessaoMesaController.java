package br.com.qrserve.controllers;

import br.com.qrserve.models.dto.form.AcessarSessaoMesaForm;
import br.com.qrserve.models.dto.response.AcessarSessaoMesaResponse;
import br.com.qrserve.services.SessaoMesaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class SessaoMesaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessaoMesaController.class);

    private final SessaoMesaService service;

    public SessaoMesaController(SessaoMesaService service) {
        this.service = service;
    }

    @PostMapping
    public AcessarSessaoMesaResponse acessar(@Valid @RequestBody AcessarSessaoMesaForm form) {
        LOGGER.info("[POST]/session: " + form.toString());
        return service.acessar(form);
    }

}
