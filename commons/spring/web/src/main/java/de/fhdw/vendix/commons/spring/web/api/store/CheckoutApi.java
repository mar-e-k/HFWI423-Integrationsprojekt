package de.fhdw.vendix.commons.spring.web.api.store;

import de.fhdw.vendix.commons.api.utility.checkout.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.utility.checkout.CheckoutResponseDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/checkout")
@Tag(name = "Checkout", description = "Operations related to checkout.")
@SecurityRequirement(name = "keycloakAuth")
public interface CheckoutApi {

    @Operation(
            summary = "Perform a checkout operation",
            description = "Processes a checkout request, including payment and receipt generation."
    )
    @PostExchange
    ResponseEntity<CheckoutResponseDTO> checkout(@RequestBody CheckoutRequestDTO request);

    @Operation(
            summary = "Print a receipt",
            description = "Generates and returns a printable version of a receipt by its ID."
    )
    @PostExchange("/{id}/print")
    ResponseEntity<ReceiptDTO> printReceipt(@PathVariable Long id);

    @Operation(
            summary = "Cancel a receipt",
            description = "Cancels an existing receipt by its ID."
    )
    @PostExchange("/{id}/cancel")
    ResponseEntity<ReceiptDTO> cancelReceipt(@PathVariable Long id);
}