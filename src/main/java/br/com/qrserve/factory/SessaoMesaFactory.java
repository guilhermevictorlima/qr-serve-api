package br.com.qrserve.factory;

import br.com.qrserve.config.TimeConfig;
import br.com.qrserve.models.data.Mesa;
import br.com.qrserve.models.data.SessaoMesa;
import br.com.qrserve.repositories.SessaoMesaRepository;
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

        return sessaoRepository.save(new SessaoMesa(new Mesa(mesaId), tokenSessao, now));
    }

    private String gerarToken(Integer mesaId, LocalDateTime now) {
        String baseToken = RandomStringUtils.randomAlphanumeric(TAMANHO_TOKEN_SESSAO);
        return baseToken + mesaId + now.format(DateTimeFormatter.ofPattern("ddMMyy"));
    }

}
