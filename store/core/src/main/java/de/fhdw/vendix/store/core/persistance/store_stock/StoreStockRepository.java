package de.fhdw.vendix.store.core.persistance.store_stock;

import de.fhdw.vendix.store.core.persistance.article.Article;
import de.fhdw.vendix.store.core.persistance.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

interface StoreStockRepository extends JpaRepository<StoreStock, Long> {
    Optional<StoreStock> findByStoreIDAndArticleID(long storeID, long articleID);

    Set<Store> findAllStoresByArticle(long articleID);

    Set<Article> findAllArticlesByStore(long storeID);
}