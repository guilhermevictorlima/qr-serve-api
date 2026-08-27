package br.com.qrserve.repositories;

import br.com.qrserve.models.data.SessaoMesa;
import br.com.qrserve.models.data.SolicitacaoEntradaSessao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoEntradaSessaoRepository extends JpaRepository<SolicitacaoEntradaSessao, Integer> {
}
