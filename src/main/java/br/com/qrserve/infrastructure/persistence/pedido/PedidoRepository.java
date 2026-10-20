package br.com.qrserve.infrastructure.persistence.pedido;

import br.com.qrserve.domain.pedido.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
}