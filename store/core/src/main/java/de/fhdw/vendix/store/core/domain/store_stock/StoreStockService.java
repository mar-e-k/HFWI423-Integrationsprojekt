package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.spring.data.persistance.service.CrudService;

import java.util.Optional;

public interface StoreStockService extends CrudService<StoreStock, Long> {
    Optional<StoreStock> findByStoreIdAndArticleId(Long storeId, Long articleId);

    void restockArticle(Long storeId, Long articleId, Long articleQuantity);
}