package com.example.application.amqp.storeEvents;

import io.github.plaguv.amqp.api.event.pos.ArticleOrderEvent;
import io.github.plaguv.amqp.api.event.pos.ArticleUrgentOrderEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AMQP Event Listener für Bestellungen von Filialen.
 *
 * Diese Komponente empfängt Bestellungen von den Filialen (über die Kasse):
 * - LogisticArticleOrder: Normale wöchentliche Bestellung bei Unterbestand
 * - LogisticUrgentArticleOrder: Sonderkommission bei kritischem Bestand (< 5 Artikel)
 */
@Component
public class LogisticOrderListener {

    private static final Logger logger = LoggerFactory.getLogger(LogisticOrderListener.class);
    private final LogisticEventPublisher logisticEventPublisher;

    public LogisticOrderListener(LogisticEventPublisher LogisticEventPublisher) {
        this.logisticEventPublisher = LogisticEventPublisher;
    }

    /**
     * Behandelt normale Bestellungen (wöchentlich, bei unter Mindestbestand 50).
     *
     * @param order Die Bestellanforderung mit storeId, articleId und Menge
     */
    @AmqpEventListener
    public void onLogisticArticleOrder(ArticleOrderEvent order) {
        if(order==null){
            throw new MessageRejectedException("Bestellung war nur ein leerer Sack, smallest order ever frfr");
        }
        logger.info("📦 Normale Bestellung erhalten - StoreID: {}, ArticleID: {}, Quantity: {}",
                order.storeId(), order.articleId(), order.quantity());

        // TODO: Implementieren
        // - Bestellung in Logistik-System registrieren
        // - Bestandsreservierung durchführen
        // - Picking-Liste erstellen
        // - Lieferstatus tracken

        logisticEventPublisher.publishArticleDelivery(order.storeId(), order.articleId(), order.quantity());
    }

    /**
     * Behandelt Sonderkommissionen (bei unter 5 Artikeln am selben Tag).
     *
     * @param order Die Sonderbestellung mit storeId, articleId und Menge
     */
    @AmqpEventListener
    public void onLogisticUrgentArticleOrder(ArticleUrgentOrderEvent order) {
        if(order==null){
            throw new MessageRejectedException("Bestellung war nur ein leerer Sack, smallest order ever frfr");
        }
        logger.info("🚨 Dringende Bestellung erhalten - StoreID: {}, ArticleID: {}, Quantity: {}",
                order.storeId(), order.articleId(), order.quantity());

        // TODO: Implementieren
        // - Bestellung mit hoher Priorität registrieren
        // - Sofortige Picking priorisieren
        // - Express-Versand einplanen
        // - Filiale benachrichtigen (über Lieferungsveröffentlichung)

        logisticEventPublisher.publishArticleDelivery(order.storeId(), order.articleId(), order.quantity());
    }
}
