package com.example.application.amqp.einkaufEvents;

import io.github.plaguv.amqp.api.envelope.EventEnvelope;
import io.github.plaguv.amqp.api.envelope.EventEnvelopeBuilder;
import io.github.plaguv.amqp.api.event.logistic.NewDealEvent;
import io.github.plaguv.amqp.core.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * AMQP Event Publisher für Events zum Einkauf-System.
 *
 * Diese Komponente publisht Events an das Einkauf-Service:
 * - NewDealEvent: Neuer Deal/Angebot für einen Artikel wurde identifiziert
 */
@Service
public class EinkaufEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EinkaufEventPublisher.class);

    private final EventPublisher eventPublisher;

    public EinkaufEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publisht einen neuen Deal/Angebot an das Einkauf-System.
     *
     * @param articleId Die Artikel-ID für den Deal
     */
    public void publishNewDeal(long articleId) {
        logger.info("📢 Neuer Deal wird publisht - ArticleID: {}",
                articleId);

        try {
            NewDealEvent event = new NewDealEvent(articleId);
            EventEnvelope envelope = EventEnvelopeBuilder.defaults()
                    .withContent(event)
                    .build();
            eventPublisher.publishMessage(envelope);

            logger.info("✅ NewDealEvent erfolgreich versendet - ArticleID: {}",
                    articleId);
        } catch (Exception e) {
            logger.error("❌ Fehler beim Versenden von NewDealEvent - ArticleID: {}, Error: {}",
                    articleId, e.getMessage(), e);
        }
    }
}

