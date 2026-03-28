package com.example.application.amqp.einkaufEvents;

import io.github.plaguv.amqp.api.event.payment.DeleteQuotaEvent;
import io.github.plaguv.amqp.api.event.payment.NewQuotaEvent;
import io.github.plaguv.amqp.core.listener.AmqpEventListener;
import io.github.plaguv.amqp.core.listener.MessageRejectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AMQP Event Listener für Events vom Einkauf-System.
 *
 * Diese Komponente empfängt Budget- und Quota-Events vom Einkauf-Service:
 * - NewQuotaEvent: Neue Quote/Budget für einen Artikel wurde freigegeben
 * - DeleteQuotaEvent: Quote/Budget für einen Artikel wurde gelöscht/aufgehoben
 */
@Component
public class EinkaufEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EinkaufEventListener.class);

    public EinkaufEventListener() {
    }

    /**
     * Behandelt neue Quote/Budget-Freigabe vom Einkauf-System.
     *
     * @param event Das NewQuotaEvent mit articleId und verfügbarem Budget
     */
    @AmqpEventListener
    public void onNewQuotaEvent(NewQuotaEvent event) {
        if (event == null) {
            throw new MessageRejectedException("NewQuotaEvent war null - ungültiges Event");
        }

        logger.info("💰 Neue Quote/Budget erhalten - ArticleID: {}, Amount: {}",
                event.articleId(), event.amount());

        // TODO: Implementieren
        // - Quote im Logistik-System registrieren
        // - Verfügbares Budget für Artikel aktualisieren
        // - Bestandsverwaltung an neue Quote anpassen
        // - Evtl. Bestellmenge-Limits neu berechnen
        // - Quote-Logging in Audit-Trail speichern
    }

    /**
     * Behandelt Quota-Löschung/Aufhebung vom Einkauf-System.
     *
     * @param event Das DeleteQuotaEvent mit ArticleId
     */
    @AmqpEventListener
    public void onDeleteQuotaEvent(DeleteQuotaEvent event) {
        if (event == null) {
            throw new MessageRejectedException("DeleteQuotaEvent war null - ungültiges Event");
        }

        logger.info("🗑️ Quote gelöscht/aufgehoben - ArticleID: {}",
                event.articleId());

        // TODO: Implementieren
        // - Quote im Logistik-System als gelöscht markieren
        // - Verfügbares Budget für Artikel auf 0 setzen
        // - Laufende Bestellungen für diesen Artikel prüfen
        // - Benutzer benachrichtigen wenn Quote über Limits hinaus war
        // - Quote-Löschung in Audit-Trail protokollieren
    }
}

