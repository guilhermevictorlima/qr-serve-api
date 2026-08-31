package br.com.qrserve.repositories;

import br.com.qrserve.models.data.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
}