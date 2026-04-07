package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.structure.service.CrudService;

import java.util.Optional;

public interface StoreStockService extends CrudService<StoreStock, Long> {
    Optional<StoreStock> findByStoreIDAndArticleID(Long storeId, Long articleId);

    void restockArticle(Long storeID, Long articleID, Long articleQuantity);
}