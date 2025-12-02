package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreLinkStockRepository extends JpaRepository<StoreLinkStock, Long> {
    boolean existsByStore(Store store);
    Optional<StoreLinkStock> findByStoreAndArticle(Store store, Article article);
}
