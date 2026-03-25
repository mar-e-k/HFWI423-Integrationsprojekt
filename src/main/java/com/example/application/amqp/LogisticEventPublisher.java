package com.example.application.amqp;

import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.logistic.ArticleSentEvent;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
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

            // TODO: Implementieren sobald Event-Klasse von Plaguv verfügbar ist
            // - Event-Klasse: LogisticArticleDelivery oder ähnlich
            // - Mit storeId, articleId, quantity
            // - Scope: Gezielte Lieferung an spezifische Filiale (EventScope.DIRECT)
            // - Wildcard: String.valueOf(storeId) für gezieltes Routing

            logger.info("📤 Lieferung schicken - StoreID: {}, ArticleID: {}, Quantity: {} " +
                    "(Event-Klasse noch nicht verfügbar)", storeId, articleId, quantity);

            ArticleSentEvent articleSentEvent = new ArticleSentEvent(storeId, articleId, quantity);

            EventEnvelope eventEnvelope = EventEnvelopeBuilder.defaults().withContent(articleSentEvent).build();

            eventPublisher.publishMessage(eventEnvelope);

    }
}
