package br.com.qrserve.controllers;

import br.com.qrserve.models.dto.form.AcessarSessaoMesaForm;
import br.com.qrserve.models.dto.response.AcessarSessaoMesaResponse;
import br.com.qrserve.services.SessaoMesaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/session")
public class SessaoMesaController {

    private final SessaoMesaService service;

    public SessaoMesaController(SessaoMesaService service) {
        this.service = service;
    }

    @PostMapping
    public AcessarSessaoMesaResponse acessar(@Valid @RequestBody AcessarSessaoMesaForm form) {
        return service.acessar(form);
    }

}
