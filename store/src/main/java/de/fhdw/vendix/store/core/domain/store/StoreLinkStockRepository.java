package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.store.core.domain.article.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreLinkStockRepository extends JpaRepository<StoreLinkStock, Long> {
    boolean existsByStore(Store store);
    Optional<StoreLinkStock> findByStoreAndArticle(Store store, Article article);
}
