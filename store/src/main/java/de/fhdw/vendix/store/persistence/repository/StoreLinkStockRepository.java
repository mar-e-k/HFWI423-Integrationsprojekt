package de.fhdw.vendix.store.persistence.repository;

import de.fhdw.vendix.store.persistence.entity.Store;
import de.fhdw.vendix.store.persistence.entity.StoreLinkStock;
import de.fhdw.vendix.store.persistence.entity.imported.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreLinkStockRepository extends JpaRepository<StoreLinkStock, Long> {
    boolean existsByStore(Store store);
    Optional<StoreLinkStock> findByStoreAndArticle(Store store, Article article);
}
