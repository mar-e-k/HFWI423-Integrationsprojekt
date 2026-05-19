package fhdw.de.einkauf_service.amqp;

/**
 * Port (Schnittstelle) für das Veröffentlichen von Domain-Events nach außen.
 * Trennt die fachliche Service-Schicht von der konkreten AMQP-Implementierung
 * und ermöglicht Tests gegen Mock-Implementierungen sowie spätere Wechsel
 * der Messaging-Technologie ohne Änderungen an den Konsumenten.
 */
public interface EventPublisherPort {

    /**
     * Veröffentlicht ein Event, dass für einen Artikel ein neues Kontingent
     * angelegt wurde.
     *
     * @param articleId fachliche Artikel-ID
     * @param amount    Menge des neuen Kontingents
     */
    void publishNewQuota(long articleId, long amount);

    /**
     * Veröffentlicht ein Event, dass das Kontingent eines Artikels entfernt
     * wurde.
     *
     * @param articleId fachliche Artikel-ID
     */
    void publishDeleteQuota(long articleId);
}
