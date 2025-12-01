package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLinkStockRepository extends JpaRepository<StoreLinkStock, Long> {

    boolean existsByStore(Store store);
}
