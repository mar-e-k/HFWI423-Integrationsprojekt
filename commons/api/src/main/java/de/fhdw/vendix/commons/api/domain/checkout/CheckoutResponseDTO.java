package de.fhdw.vendix.commons.api.domain.checkout;

import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Antwort auf einen erfolgreichen Checkout.
 * Enthält die erstellte Receipt-ID und optional die gespeicherten Bon-Positionen.
 *
 * @param receiptId   ID des neu erstellten Bons in der Datenbank
 * @param storeId     Store-ID zur Bestätigung
 * @param registerId  Kassen-ID zur Bestätigung
 * @param cashierUuid   Kassierer-UUID zur Bestätigung
 * @param lineCount   Anzahl der erstellten Bon-Positionen
 * @param lineIds     IDs der erstellten ReceiptLine-Einträge; leer, wenn im Request abgewählt
 */
public record CheckoutResponseDTO(
        Long receiptId,
        Long storeId,
        Long registerId,
        UUID cashierUuid,
        int lineCount,
        List<Long> lineIds
) implements ResponseDTO {}