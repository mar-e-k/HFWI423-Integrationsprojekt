package de.fhdw.vendix.commons.spring.web.server.store.api;

import de.fhdw.vendix.commons.api.domain.receipt.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutResponseDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * OpenAPI-generiertes Interface für Receipt-Operationen.
 *
 * Entspricht den Pfaden in store.yaml:
 *   POST /api/receipt/checkout   → checkout()
 *   POST /api/receipt/{id}/print → printReceipt()
 *   POST /api/receipt/{id}/cancel → cancelReceipt()
 */
@RequestMapping("/api/receipt")
public interface CheckoutApi {

    /**
     * POST /api/receipt/checkout
     * Erstellt einen Bon mit allen Positionen in einer Transaktion.
     */
    @PostMapping("/checkout")
    ResponseEntity<CheckoutResponseDTO> checkout(@RequestBody CheckoutRequestDTO request);

    /**
     * POST /api/receipt/{id}/print
     * Bon abschließen: OPEN → PRINTED.
     */
    @PostMapping("/{id}/print")
    ResponseEntity<ReceiptDTO> printReceipt(@PathVariable Long id);

    /**
     * POST /api/receipt/{id}/cancel
     * Bon stornieren: OPEN → CANCELLED.
     */
    @PostMapping("/{id}/cancel")
    ResponseEntity<ReceiptDTO> cancelReceipt(@PathVariable Long id);
}