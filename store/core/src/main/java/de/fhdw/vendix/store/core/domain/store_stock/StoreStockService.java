package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.spring.data.persistance.service.CrudService;

import java.util.Map;
import java.util.Optional;

public interface StoreStockService extends CrudService<StoreStock, Long> {
    Optional<StoreStock> findByStoreIdAndArticleId(Long storeId, Long articleId);

    void restockArticle(Long storeId, Long articleId, Long articleQuantity);

    void decrementArticles(Long storeId, Map<Long, Long> amountByArticleId);

    void incrementArticles(Long storeId, Map<Long, Long> amountByArticleId);
}