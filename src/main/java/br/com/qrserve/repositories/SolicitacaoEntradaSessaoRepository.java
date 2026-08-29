package br.com.qrserve.repositories;

import br.com.qrserve.models.data.SolicitacaoEntradaSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SolicitacaoEntradaSessaoRepository extends JpaRepository<SolicitacaoEntradaSessao, Integer> {

    @Query(value = """
            select * from solicitacao_entrada_sessao where token = ?1
            """, nativeQuery = true)
    Optional<SolicitacaoEntradaSessao> obterSolicitacaoPorToken(String tokenUsuario);


}
