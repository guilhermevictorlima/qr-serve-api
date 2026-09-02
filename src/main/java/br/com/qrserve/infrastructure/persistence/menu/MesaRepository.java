package br.com.qrserve.infrastructure.persistence.menu;

import br.com.qrserve.domain.mesa.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {
}
