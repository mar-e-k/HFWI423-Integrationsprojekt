package de.fhdw.vendix.commons.spring.web.api.store;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/receipt")
@Tag(name = "Receipt", description = "Operations related to receipts.")
@SecurityRequirement(name = "keycloakAuth")
public interface ReceiptApi {

    @Operation(
            summary = "Create a new receipt (header only)",
            description = "Creates a new receipt record without any line items."
    )
    @PostExchange()
    ResponseEntity<ReceiptDTO> postReceipt(@RequestBody ReceiptDTO receiptDTO);
}