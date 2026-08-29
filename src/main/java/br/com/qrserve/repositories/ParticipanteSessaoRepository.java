package br.com.qrserve.repositories;

import br.com.qrserve.models.data.ParticipanteSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteSessaoRepository extends JpaRepository<ParticipanteSessao, Integer> {

    @Query(value = """
            select * from participante_sessao where token = ?1
            """, nativeQuery = true)
    Optional<ParticipanteSessao> obterParticipantePorToken(String tokenUsuario);

    @Query(value = """
            select * from participante_sessao where sessao_id = ?1
            """, nativeQuery = true)
    List<ParticipanteSessao> listarParticipantes(Integer sessaoId);

}
