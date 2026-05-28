package de.fhdw.vendix.commons.api.domain.checkout;

import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * Vollständige Checkout-Anfrage: Erstellt einen Bon mit allen Positionen in
 * einer einzigen Transaktion.
 *
 * Endpunkt: POST /api/receipt/checkout
 *
 * @param storeId       ID des Stores
 * @param registerId    ID der Kasse
 * @param cashierUUID   UUID des Kassierers
 * @param paymentMethod Zahlungsmethode (CASH, CARD, ONLINE)
 * @param lines         Bon-Positionen (min. 1 Artikel)
 * @param returnLineIds optional; false keeps the response small for load tests
 */
public record CheckoutRequestDTO(
        Long storeId,
        Long registerId,
        UUID cashierUUID,
        PaymentMethod paymentMethod,
        List<CheckoutLineDTO> lines,
        Boolean returnLineIds
) implements RequestDTO {

    public CheckoutRequestDTO {
        if (storeId == null || storeId < 1) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'storeId' muss >= 1 sein");
        }
        if (registerId == null || registerId < 1) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'registerId' muss >= 1 sein");
        }
        if (cashierUUID == null) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'cashierUUId' cannot be null");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'paymentMethod' darf nicht null sein");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'lines' darf nicht leer sein");
        }
        returnLineIds = returnLineIds == null || returnLineIds;
    }
}
