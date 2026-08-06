package br.com.qrserve.services;

import br.com.qrserve.models.data.ParticipanteSessao;
import br.com.qrserve.models.data.SessaoMesa;
import br.com.qrserve.models.dto.SessaoMesaAtiva;
import br.com.qrserve.models.dto.form.AcessarSessaoMesaForm;
import br.com.qrserve.models.dto.response.AcessarSessaoMesaResponse;
import br.com.qrserve.repositories.ParticipanteSessaoRepository;
import br.com.qrserve.repositories.SessaoMesaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessaoMesaService {

    private static final int TEMPO_LIMITE_EM_MINUTOS_SESSAO_ABERTA = 3;

    private final SessaoMesaRepository sessaoRepository;
    private final ParticipanteSessaoRepository participanteSessaoRepository;

    public SessaoMesaService(SessaoMesaRepository sessaoRepository, ParticipanteSessaoRepository participanteSessaoRepository) {
        this.sessaoRepository = sessaoRepository;
        this.participanteSessaoRepository = participanteSessaoRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public AcessarSessaoMesaResponse acessar(AcessarSessaoMesaForm form) {
        final String tokenUsuario = UUID.randomUUID().toString();

        Integer sessaoId = sessaoRepository.obterSessaoAtiva(form.mesaId())
                .map(sessao -> {
                    this.tentarEntrarNaSessao(sessao, form, tokenUsuario);
                    return sessao.id();
                })
                .orElse(this.criarSessao(tokenUsuario));

        return new AcessarSessaoMesaResponse(tokenUsuario, sessaoId);
    }

    private void tentarEntrarNaSessao(SessaoMesaAtiva sessao, AcessarSessaoMesaForm form, String tokenUsuario) {
        boolean jaPassouTempoLimiteDeSessaoAberta = LocalDateTime.now().isAfter(sessao.dataHoraInicio().plusMinutes(TEMPO_LIMITE_EM_MINUTOS_SESSAO_ABERTA));
        if (jaPassouTempoLimiteDeSessaoAberta) {
            // TODO solicitarEntrada
        } else {
            participanteSessaoRepository.save(new ParticipanteSessao(new SessaoMesa(sessao.id()), tokenUsuario, form.nome()));
        }

    }

    private Integer criarSessao(String tokenUsuario) {
        // TODO chamar Factory
        return 0;
    }
}
