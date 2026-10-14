package br.com.qrserve.application.sessao;

import br.com.qrserve.application.time.TimeProvider;
import br.com.qrserve.domain.exception.MesaNaoEncontradaException;
import br.com.qrserve.domain.mesa.Mesa;
import br.com.qrserve.domain.sessao.ParticipanteSessao;
import br.com.qrserve.domain.sessao.SessaoMesa;
import br.com.qrserve.domain.sessao.SolicitacaoEntradaSessao;
import br.com.qrserve.domain.sessao.TokenUsuario;
import br.com.qrserve.infrastructure.persistence.mesa.MesaRepository;
import br.com.qrserve.infrastructure.persistence.sessao.ParticipanteSessaoRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SessaoMesaRepository;
import br.com.qrserve.infrastructure.persistence.sessao.SolicitacaoEntradaSessaoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AcessarSessaoUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcessarSessaoUseCase.class);

    private final MesaRepository mesaRepository;
    private final SessaoMesaRepository repository;
    private final TimeProvider timeProvider;
    private final SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository;
    private final ParticipanteSessaoRepository participanteSessaoRepository;

    public AcessarSessaoUseCase(MesaRepository mesaRepository, SessaoMesaRepository repository, TimeProvider timeProvider, SolicitacaoEntradaSessaoRepository solicitacaoEntradaSessaoRepository, ParticipanteSessaoRepository participanteSessaoRepository) {
        this.mesaRepository = mesaRepository;
        this.repository = repository;
        this.timeProvider = timeProvider;
        this.solicitacaoEntradaSessaoRepository = solicitacaoEntradaSessaoRepository;
        this.participanteSessaoRepository = participanteSessaoRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public AcessarSessaoResult execute(AcessarSessaoCommand command) {
        TokenUsuario token = TokenUsuario.create();

        Mesa mesa = obterMesa(command.mesaId());

        SessaoMesa sessao = repository.obterSessaoAtiva(mesa.getId())
                .orElseGet(() -> repository.save(SessaoMesa.create(mesa, timeProvider.dataHoraAtual())));

        StatusAcessoSessao responseStatus = tentarEntrarNaSessao(sessao, token, command.nome());

        return new AcessarSessaoResult(token.getValor(), sessao.getId(), responseStatus);
    }

    private Mesa obterMesa(Integer mesaId) {
        return mesaRepository.obterParaAtualizar(mesaId)
                .orElseThrow(() -> {
                    LOGGER.error("MESA NÃO ENCONTRADA -- {}", mesaId);
                    return new MesaNaoEncontradaException("Mesa não encontrada, verifique o QR Code e tente novamente");
                });
    }

    private StatusAcessoSessao tentarEntrarNaSessao(SessaoMesa sessao, TokenUsuario tokenUsuario, String nomeUsuario) {
        LocalDateTime dataHoraAtual = timeProvider.dataHoraAtual();

        if (sessao.isPermitidoEntrarNaSessao(dataHoraAtual)) {
            participanteSessaoRepository.save(new ParticipanteSessao(sessao, tokenUsuario, nomeUsuario));
            return StatusAcessoSessao.ACESSO_LIBERADO;
        }

        solicitacaoEntradaSessaoRepository.save(new SolicitacaoEntradaSessao(sessao, tokenUsuario, nomeUsuario, dataHoraAtual));
        return StatusAcessoSessao.PERMISSAO_REQUERIDA;
    }
}
