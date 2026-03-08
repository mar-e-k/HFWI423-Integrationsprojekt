package com.example.application.amqp.events;

/**
 * Temporäre Event-Klasse für LogisticArticleOrder.
 * Diese sollte eigentlich aus io.github.plaguv.contract.event.logistic kommen,
 * aber ist in der aktuellen Library-Version nicht verfügbar.
 *
 * Normale wöchentliche Bestellung bei Unterbestand (50 Artikel).
 */
public record LogisticArticleOrder(
        long storeId,
        long articleId,
        long quantity
) {
    public LogisticArticleOrder {
        if (storeId < 1) {
            throw new IllegalArgumentException("storeId must be greater than 0.");
        }
        if (articleId < 1) {
            throw new IllegalArgumentException("articleId must be greater than 0.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be greater than 0.");
        }
    }
}
