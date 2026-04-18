package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutResponseDTO;

/**
 * Koordiniert den vollständigen Kassenabschluss:
 * Bon anlegen + alle Positionen speichern in einer Transaktion.
 */
public interface CheckoutService {

    /**
     * Erstellt einen Bon mit allen übergebenen Positionen atomisch.
     *
     * @param request Checkout-Request mit Store/Kasse/Kassierer + Artikel-Positionen
     * @return Response mit Receipt-ID und den IDs der gespeicherten Positionen
     */
    CheckoutResponseDTO checkout(CheckoutRequestDTO request);
}