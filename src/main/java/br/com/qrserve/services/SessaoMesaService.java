package br.com.qrserve.services;

import br.com.qrserve.factory.SessaoMesaFactory;
import br.com.qrserve.models.data.ParticipanteSessao;
import br.com.qrserve.models.data.SessaoMesa;
import br.com.qrserve.models.data.SolicitacaoEntradaSessao;
import br.com.qrserve.models.dto.form.AcessarSessaoMesaForm;
import br.com.qrserve.models.dto.response.AcessarSessaoMesaResponse;
import br.com.qrserve.models.dto.response.AcessarSessaoMesaResponseStatus;
import br.com.qrserve.repositories.ParticipanteSessaoRepository;
import br.com.qrserve.repositories.SolicitacaoEntradaSessaoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.text.MessageFormat.format;

@Service
public class SessaoMesaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessaoMesaService.class);

    private static final int TEMPO_LIMITE_EM_MINUTOS_SESSAO_ABERTA = 3;

    private final SessaoMesaFactory sessaoMesaFactory;
    private final ParticipanteSessaoRepository participanteSessaoRepository;
    private final SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;

    public SessaoMesaService(SessaoMesaFactory sessaoMesaFactory, ParticipanteSessaoRepository participanteSessaoRepository, SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository) {
        this.sessaoMesaFactory = sessaoMesaFactory;
        this.participanteSessaoRepository = participanteSessaoRepository;
        this.solicitacaoEntradaSessaoRepository = solicitacaoEntradaSessaoRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public AcessarSessaoMesaResponse acessar(AcessarSessaoMesaForm form) {
        final String tokenUsuario = UUID.randomUUID().toString();

        SessaoMesa sessao = sessaoMesaFactory.obter(form.mesaId());
        AcessarSessaoMesaResponseStatus responseStatus = tentarEntrarNaSessao(sessao, form, tokenUsuario);

        return new AcessarSessaoMesaResponse(tokenUsuario, sessao.getId(), responseStatus);
    }

    private AcessarSessaoMesaResponseStatus tentarEntrarNaSessao(SessaoMesa sessaoMesa, AcessarSessaoMesaForm form, String tokenUsuario) {
        boolean jaPassouTempoLimiteDeSessaoAberta = LocalDateTime.now().isAfter(sessaoMesa.getDataHoraInicio().plusMinutes(TEMPO_LIMITE_EM_MINUTOS_SESSAO_ABERTA));
        if (jaPassouTempoLimiteDeSessaoAberta) {
            LOGGER.warn(format("TEMPO DE ENTRADA LIVRE NA SESSÃO EXPIRADO -- {0} / tokenUsuario: {1}", form, tokenUsuario));
            solicitacaoEntradaSessaoRepository.save(new SolicitacaoEntradaSessao(sessaoMesa, tokenUsuario, form.nome()));
            return AcessarSessaoMesaResponseStatus.PERMISSAO_REQUERIDA;
        } else {
            participanteSessaoRepository.save(new ParticipanteSessao(sessaoMesa, tokenUsuario, form.nome()));
            return AcessarSessaoMesaResponseStatus.ACESSO_LIBERADO;
        }

    }
}
