package br.com.qrserve.presentation.mesa;

import br.com.qrserve.presentation.mesa.response.MesaResponse;
import br.com.qrserve.infrastructure.persistence.menu.MesaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mesas")
public class MesaController {

    private final MesaRepository repository;

    public MesaController(MesaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MesaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(MesaResponse::from)
                .toList();
    }

}
