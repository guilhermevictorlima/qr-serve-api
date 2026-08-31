package br.com.qrserve.infrastructure.persistence.sessao;

import br.com.qrserve.domain.sessao.SessaoMesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessaoMesaRepository extends JpaRepository<SessaoMesa, Integer> {

    @Query(value = """
            SELECT * FROM sessao_mesa where mesa_id = ?1 and data_hora_encerramento is null
            """, nativeQuery = true)
    Optional<SessaoMesa> obterSessaoAtiva(Integer mesaId);
}
