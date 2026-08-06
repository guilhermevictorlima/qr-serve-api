package br.com.qrserve.repositories;

import br.com.qrserve.models.data.ParticipanteSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipanteSessaoRepository extends JpaRepository<ParticipanteSessao, Integer> {
}
