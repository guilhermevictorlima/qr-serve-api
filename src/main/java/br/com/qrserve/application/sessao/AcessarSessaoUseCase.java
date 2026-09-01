package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.domain.sessao.TokenUsuario;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SessaoMesaRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import br.com.qrserve.presentation.sessao.form.AcessarSessaoMesaForm;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponse;
import br.com.qrserve.presentation.sessao.response.AcessarSessaoMesaResponseStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AcessarSessaoUseCase {
    private final SessaoMesaRepository repository;
    private final TimeProvider timeProvider;
    private final SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;
    private final ParticipanteSessaoRepository participanteSessaoRepository;

    public AcessarSessaoUseCase(SessaoMesaRepository repository, TimeProvider timeProvider, SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository, ParticipanteSessaoRepository participanteSessaoRepository) {
        this.repository = repository;
        this.timeProvider = timeProvider;
        this.solicitacaoEntradaSessaoRepository = solicitacaoEntradaSessaoRepository;
        this.participanteSessaoRepository = participanteSessaoRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public AcessarSessaoMesaResponse execute(AcessarSessaoMesaForm form) {
        TokenUsuario token = TokenUsuario.create();

        SessaoMesa sessao = repository.obterSessaoAtiva(form.mesaId())
                .orElseGet(() -> repository.save(SessaoMesa.create(form.mesaId(), timeProvider.dataHoraAtual())));

        AcessarSessaoMesaResponseStatus responseStatus = tentarEntrarNaSessao(sessao, token, form.nome());

        return new AcessarSessaoMesaResponse(token.getValor(), sessao.getId(), responseStatus);
    }

    private AcessarSessaoMesaResponseStatus tentarEntrarNaSessao(SessaoMesa sessao, TokenUsuario tokenUsuario, String nomeUsuario) {
        LocalDateTime dataHoraAtual = timeProvider.dataHoraAtual();

        if (sessao.isPermitidoEntrarNaSessao(dataHoraAtual)) {
            participanteSessaoRepository.save(new ParticipanteSessao(sessao, tokenUsuario, nomeUsuario));
            return AcessarSessaoMesaResponseStatus.ACESSO_LIBERADO;
        }

        solicitacaoEntradaSessaoRepository.save(new SolicitacaoEntradaSessao(sessao, tokenUsuario, nomeUsuario, dataHoraAtual));
        return AcessarSessaoMesaResponseStatus.PERMISSAO_REQUERIDA;
    }
}
