package br.com.qrserve.repositories;

import br.com.qrserve.models.data.SessaoMesa;
import br.com.qrserve.models.dto.SessaoMesaAtiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessaoMesaRepository extends JpaRepository<SessaoMesa, Integer> {

    @Query(value = """
            SELECT id, data_hora_inicio FROM sessao_mesa where mesa_id = ?1 and data_hora_encerramento is null
            """, nativeQuery = true)
    Optional<SessaoMesaAtiva> obterSessaoAtiva(Integer mesaId);
}
