package com.example.application.amqp.events;

/**
 * Temporäre Event-Klasse für LogisticUrgentArticleOrder.
 * Diese sollte eigentlich aus io.github.plaguv.contract.event.logistic kommen,
 * aber ist in der aktuellen Library-Version nicht verfügbar.
 *
 * Sonderkommission bei kritischem Bestand (< 5 Artikel am selben Tag).
 */
public record LogisticUrgentArticleOrder(
        long storeId,
        long articleId,
        long quantity
) {
    public LogisticUrgentArticleOrder {
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
