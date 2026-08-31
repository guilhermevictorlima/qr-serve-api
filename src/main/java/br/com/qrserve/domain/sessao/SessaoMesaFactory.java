package br.com.qrserve.domain.sessao;

import br.com.qrserve.infrastructure.config.TimeConfig;
import br.com.qrserve.domain.mesa.Mesa;
import br.com.qrserve.infrastructure.persistence.sessao.SessaoMesaRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SessaoMesaFactory {

    private static final int TAMANHO_TOKEN_SESSAO = 10;

    private final SessaoMesaRepository sessaoRepository;
    private final TimeConfig timeConfig;

    public SessaoMesaFactory(SessaoMesaRepository sessaoRepository, TimeConfig timeConfig) {
        this.sessaoRepository = sessaoRepository;
        this.timeConfig = timeConfig;
    }

    @Transactional
    public SessaoMesa obter(Integer mesaId) {
        return sessaoRepository.obterSessaoAtiva(mesaId).orElseGet(() -> this.criar(mesaId));
    }

    private SessaoMesa criar(Integer mesaId) {
        LocalDateTime now = timeConfig.dataHoraAtual();
        final String tokenSessao = gerarToken(mesaId, now);

        return sessaoRepository.save(new SessaoMesa(new Mesa(mesaId), tokenSessao, now)); // TODO domain não deve se preocupar com persistência, somente com criação
    }

    private String gerarToken(Integer mesaId, LocalDateTime now) {
        String baseToken = RandomStringUtils.randomAlphanumeric(TAMANHO_TOKEN_SESSAO);
        return baseToken + mesaId + now.format(DateTimeFormatter.ofPattern("ddMMyy"));
    }

}
