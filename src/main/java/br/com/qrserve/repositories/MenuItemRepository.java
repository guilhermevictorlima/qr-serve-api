package br.com.qrserve.repositories;

import br.com.qrserve.models.data.cardapio.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
}