package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.receipt.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutResponseDTO;
import de.fhdw.vendix.store.core.domain.receipt.CheckoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kassenabschluss-Endpunkt.
 *
 * POST /api/receipt/checkout
 *
 * Erstellt einen Bon mit allen Positionen in einer einzigen Transaktion.
 * Wird von k6-Lasttests als "vollständiger Checkout" genutzt.
 *
 * Request-Body Beispiel:
 * {
 *   "storeId":    1,
 *   "registerId": 2,
 *   "cashierId":  4,
 *   "lines": [
 *     { "articleId": 5, "articleAmount": 2, "discountPercent": null },
 *     { "articleId": 17, "articleAmount": 1, "discountPercent": 10.00 }
 *   ]
 * }
 *
 * Response-Body Beispiel (201 Created):
 * {
 *   "receiptId": 42,
 *   "storeId": 1,
 *   "registerId": 2,
 *   "cashierId": 4,
 *   "lineCount": 2,
 *   "lineIds": [101, 102]
 * }
 */
@RestController
@RequestMapping("/api/receipt")
class CheckoutController {

    private final CheckoutService checkoutService;

    CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    ResponseEntity<CheckoutResponseDTO> checkout(@RequestBody CheckoutRequestDTO request) {
        CheckoutResponseDTO response = checkoutService.checkout(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}