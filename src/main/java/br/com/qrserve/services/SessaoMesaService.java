package br.com.qrserve.services;

import br.com.qrserve.factory.SessaoMesaFactory;
import br.com.qrserve.models.data.ParticipanteSessao;
import br.com.qrserve.models.data.SessaoMesa;
import br.com.qrserve.models.dto.form.AcessarSessaoMesaForm;
import br.com.qrserve.models.dto.response.AcessarSessaoMesaResponse;
import br.com.qrserve.repositories.ParticipanteSessaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessaoMesaService {

    private static final int TEMPO_LIMITE_EM_MINUTOS_SESSAO_ABERTA = 3;

    private final SessaoMesaFactory sessaoMesaFactory;
    private final ParticipanteSessaoRepository participanteSessaoRepository;

    public SessaoMesaService(SessaoMesaFactory sessaoMesaFactory, ParticipanteSessaoRepository participanteSessaoRepository) {
        this.sessaoMesaFactory = sessaoMesaFactory;
        this.participanteSessaoRepository = participanteSessaoRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public AcessarSessaoMesaResponse acessar(AcessarSessaoMesaForm form) {
        final String tokenUsuario = UUID.randomUUID().toString();

        SessaoMesa sessao = sessaoMesaFactory.obter(form.mesaId());
        tentarEntrarNaSessao(sessao, form, tokenUsuario);

        return new AcessarSessaoMesaResponse(tokenUsuario, sessao.getId());
    }

    private void tentarEntrarNaSessao(SessaoMesa sessaoMesa, AcessarSessaoMesaForm form, String tokenUsuario) {
        boolean jaPassouTempoLimiteDeSessaoAberta = LocalDateTime.now().isAfter(sessaoMesa.getDataHoraInicio().plusMinutes(TEMPO_LIMITE_EM_MINUTOS_SESSAO_ABERTA));
        if (jaPassouTempoLimiteDeSessaoAberta) {
            // TODO solicitarEntrada
        } else {
            participanteSessaoRepository.save(new ParticipanteSessao(sessaoMesa, tokenUsuario, form.nome()));
        }

    }
}
