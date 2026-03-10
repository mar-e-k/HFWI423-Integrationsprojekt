package com.example.application.amqp;

import io.github.plaguv.core.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service für das Publishen von AMQP-Events von der Logistik an die Kasse.
 *
 * Dieser Service sendet Lieferungsbenachrichtigungen an die Filialen, wenn:
 * - Bestellte Artikel versendet werden
 * - Lieferstatus-Updates verfügbar sind
 *
 * Benötigte Event-Klassen von Plaguv:
 * - LogisticArticleDelivery (oder ähnlich): storeId, articleId, quantity
 * - LogisticDeliveryStatus (oder ähnlich): storeId, status, timestamp
 */
@Service
public class LogisticEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(LogisticEventPublisher.class);
    private final EventPublisher eventPublisher;

    public LogisticEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishen einer Liefermitteilung an eine Filiale.
     *
     * Wird aufgerufen, wenn bestellte Artikel versendet werden.
     *
     * @param storeId   Die ID der empfangenden Filiale
     * @param articleId Die ID des versendeten Artikels
     * @param quantity  Die Menge der versendeten Artikel
     */
    public void publishArticleDelivery(long storeId, long articleId, long quantity) {
        try {
            // TODO: Implementieren sobald Event-Klasse von Plaguv verfügbar ist
            // - Event-Klasse: LogisticArticleDelivery oder ähnlich
            // - Mit storeId, articleId, quantity
            // - Scope: Gezielte Lieferung an spezifische Filiale (EventScope.DIRECT)
            // - Wildcard: String.valueOf(storeId) für gezieltes Routing

            logger.info("📤 Lieferung schicken - StoreID: {}, ArticleID: {}, Quantity: {} " +
                    "(Event-Klasse noch nicht verfügbar)", storeId, articleId, quantity);

        } catch (Exception e) {
            logger.error("❌ Fehler beim Schicken der Lieferung: {}", e.getMessage(), e);
        }
    }

    /**
     * Publishen eines Lieferstatus-Updates.
     *
     * Wird aufgerufen, wenn sich der Status einer Bestellung ändert.
     *
     * @param storeId Die ID der Filiale
     * @param status  Der aktuelle Status (z.B. "IN_TRANSIT", "DELIVERED", etc.)
     */
    public void publishDeliveryStatusUpdate(long storeId, String status) {
        try {
            // TODO: Implementieren sobald Event-Klasse von Plaguv verfügbar ist
            // - Event-Klasse: LogisticDeliveryStatus oder ähnlich
            // - Mit storeId, status, timestamp
            // - Scope: Gezielte Benachrichtigung an Filiale (EventScope.DIRECT)
            // - Wildcard: String.valueOf(storeId) für gezieltes Routing

            logger.info("📤 Status-Update schicken - StoreID: {}, Status: {} " +
                    "(Event-Klasse noch nicht verfügbar)", storeId, status);

        } catch (Exception e) {
            logger.error("❌ Fehler beim Schicken des Status-Updates: {}", e.getMessage(), e);
        }
    }
}
