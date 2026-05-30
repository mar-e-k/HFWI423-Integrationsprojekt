package de.fhdw.vendix.store.core.domain.store_stock_order;

/**
 * Port for publishing stock replenishment orders without coupling the store
 * domain directly to a concrete messaging implementation.
 */
public interface ArticleOrderPublisher {

    void publishOrder(long storeId, long articleId, long amount, boolean urgent);
}
