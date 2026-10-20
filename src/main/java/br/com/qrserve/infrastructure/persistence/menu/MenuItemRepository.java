package br.com.qrserve.infrastructure.persistence.menu;

import br.com.qrserve.domain.menu.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
}