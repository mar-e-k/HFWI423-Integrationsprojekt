package de.fhdw.vendix.store.core.domain.store_stock;

import java.util.Map;

interface StoreStockBulkRepository {

    void decrementArticles(Long storeId, Map<Long, Long> amountByArticleId);

    void incrementArticles(Long storeId, Map<Long, Long> amountByArticleId);
}
