package de.fhdw.vendix.store.core.messaging;

/**
 * Secondary port: encapsulates publishing of article order events to the
 * AMQP broker. Decouples the domain (scheduler, test controller) from the
 * concrete messaging library.
 *
 * <p>Implementations live in {@code store-app} where the AMQP starter is
 * available. The interface itself stays in {@code store-core} so that
 * {@code store-web} and the scheduler can use it without a direct AMQP
 * dependency.
 */
public interface ArticleOrderMessagingService {

    /**
     * Publishes a normal {@code ArticleOrderEvent} to the central exchange.
     *
     * @param storeId   ID of the requesting store (must be &gt; 0)
     * @param articleId ID of the article to order (must be &gt; 0)
     * @param amount    quantity requested (must be &gt; 0)
     */
    void sendOrder(long storeId, long articleId, long amount);

    /**
     * Publishes an urgent {@code ArticleUrgentOrderEvent} to the central exchange.
     *
     * @param storeId   ID of the requesting store (must be &gt; 0)
     * @param articleId ID of the article to order (must be &gt; 0)
     * @param amount    quantity requested (must be &gt; 0)
     */
    void sendUrgentOrder(long storeId, long articleId, long amount);
}
