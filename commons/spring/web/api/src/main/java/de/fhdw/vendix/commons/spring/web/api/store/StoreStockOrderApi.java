package de.fhdw.vendix.commons.spring.web.api.store;

import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderStatusDTO;
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

import java.util.UUID;

@HttpExchange("/api/store-stock/order")
@Tag(
        name = "Store Stock Order",
        description = "Operations for managing stock replenishment orders."
)
@KeycloakOpenApiScheme
@StoreRoutingOpenApiScheme
public interface StoreStockOrderApi {

    @Operation(
            summary = "Get stock order status",
            description = "Retrieves the current processing status of a stock order via its correlation ID."
    )
    @GetExchange("/correlation-id/{correlationId}")
    ResponseEntity<ReplenishmentOrderStatusDTO> getStoreStockOrderStatus(@PathVariable UUID correlationId);

    @Operation(
            summary = "Request stock replenishment",
            description = "Validates a stock order and publishes an AMQP event. Returns a tracking correlation ID."
    )
    @PostExchange
    ResponseEntity<ReplenishmentOrderResponseDTO> createStoreStockOrder(@RequestBody ReplenishmentOrderRequestDTO request);
}