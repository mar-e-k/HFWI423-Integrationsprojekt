package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

import java.util.List;

/**
 * Vollständige Checkout-Anfrage: Erstellt einen Bon mit allen Positionen in
 * einer einzigen Transaktion.
 *
 * Endpunkt: POST /api/receipt/checkout
 *
 * @param storeId       ID des Stores
 * @param registerId    ID der Kasse
 * @param cashierId     ID des Kassierers
 * @param paymentMethod Zahlungsmethode (CASH, CARD, ONLINE)
 * @param lines         Bon-Positionen (min. 1 Artikel)
 */
public record CheckoutRequestDTO(
        Long storeId,
        Long registerId,
        Long cashierId,
        PaymentMethod paymentMethod,
        List<CheckoutLineDTO> lines
) implements RequestDTO {

    public CheckoutRequestDTO {
        if (storeId == null || storeId < 1) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'storeId' muss >= 1 sein");
        }
        if (registerId == null || registerId < 1) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'registerId' muss >= 1 sein");
        }
        if (cashierId == null || cashierId < 1) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'cashierId' muss >= 1 sein");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'paymentMethod' darf nicht null sein");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("CheckoutRequestDTO: 'lines' darf nicht leer sein");
        }
    }
}