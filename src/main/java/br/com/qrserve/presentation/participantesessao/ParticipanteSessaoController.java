package br.com.qrserve.presentation.participantesessao;

import br.com.qrserve.application.sessao.ListarParticipantesCommand;
import br.com.qrserve.application.sessao.ListarParticipantesUseCase;
import br.com.qrserve.presentation.participantesessao.response.ParticipanteSessaoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/participantes")
public class ParticipanteSessaoController {

    private final ListarParticipantesUseCase listarParticipantesUseCase;

    public ParticipanteSessaoController(ListarParticipantesUseCase listarParticipantesUseCase) {
        this.listarParticipantesUseCase = listarParticipantesUseCase;
    }

    @GetMapping("/{sessaoId}")
    public List<ParticipanteSessaoResponse> listar(
            @PathVariable(name = "sessaoId") Integer sessaoId,
            @RequestHeader(name = "X-Token-Usuario") String tokenUsuario
    ) {
        return listarParticipantesUseCase.execute(new ListarParticipantesCommand(sessaoId, tokenUsuario))
                .stream()
                .map(ParticipanteSessaoResponse::from)
                .toList();
    }

}
