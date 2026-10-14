package br.com.qrserve.infrastructure.persistence.mesa;

import br.com.qrserve.domain.mesa.Mesa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mesa m where m.id = ?1")
    Optional<Mesa> obterParaAtualizar(Integer mesaId);
}
