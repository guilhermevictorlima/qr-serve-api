package br.com.qrserve.controllers;

import br.com.qrserve.models.dto.response.ParticipanteSessaoResponse;
import br.com.qrserve.repositories.ParticipanteSessaoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/participantes")
public class ParticipanteSessaoController {

    private final ParticipanteSessaoRepository repository;

    public ParticipanteSessaoController(ParticipanteSessaoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{sessaoId}")
    public List<ParticipanteSessaoResponse> listar(@PathVariable(name = "sessaoId") Integer sessaoId) {
        return repository.listarParticipantes(sessaoId)
                .stream()
                .map(ParticipanteSessaoResponse::from)
                .toList();
    }

}
