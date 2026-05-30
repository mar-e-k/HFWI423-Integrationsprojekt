package de.fhdw.vendix.commons.spring.web.api.store;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptResponseDTO;
import de.fhdw.vendix.commons.spring.web.api.scheme.KeycloakOpenApiScheme;
import de.fhdw.vendix.commons.spring.web.api.scheme.StoreRoutingOpenApiScheme;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

@HttpExchange("/api/receipt")
@Tag(
        name = "Receipt",
        description = "Operations related to receipts."
)
@KeycloakOpenApiScheme
@StoreRoutingOpenApiScheme
public interface ReceiptApi {

    @Operation(
            summary = "[DNT]",
            description = "[DNT]"
    )
    @GetExchange
    ResponseEntity<List<ReceiptResponseDTO>> getReceipts();

    @Operation(
            summary = "Create a new receipt (header only)",
            description = "Creates a new receipt record without any line items."
    )
    @PostExchange
    ResponseEntity<ReceiptResponseDTO> postReceipt(@RequestBody ReceiptRequestDTO receiptRequestDTO);

    @Operation(
            summary = "Create a new receipt (header only)",
            description = "Creates a new receipt record without any line items."
    )
    @PostExchange("/checkout")
    ResponseEntity<ReceiptResponseDTO> checkoutReceipt(@RequestBody ReceiptRequestDTO receiptRequestDTO);

    @Operation(
            summary = "[DNT] Print Receipt",
            description = "[DNT] Prints Receipt"
    )
    @PutExchange("/id/{receiptId}/print")
    ResponseEntity<ReceiptResponseDTO> printReceipt(@PathVariable Long receiptId);

    @Operation(
            summary = "[DNT] Cancel Receipt",
            description = "[DNT] Cancels Receipt"
    )
    @PutExchange("/id/{receiptId}/cancel")
    ResponseEntity<ReceiptResponseDTO> cancelReceipt(@PathVariable Long receiptId);
}