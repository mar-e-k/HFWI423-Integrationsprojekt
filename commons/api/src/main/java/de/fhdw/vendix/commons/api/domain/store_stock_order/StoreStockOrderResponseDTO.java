package de.fhdw.vendix.commons.api.domain.store_stock_order;

import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

import java.util.UUID;

public record StoreStockOrderResponseDTO(
        UUID correlationId,
        OrderStatus status,
        String statusUrl
) implements ResponseDTO {

    public StoreStockOrderResponseDTO {
        if (correlationId == null) {
            throw new IllegalArgumentException("ReplenishmentOrderResponseDTO: 'correlationId' cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("ReplenishmentOrderResponseDTO: 'status' cannot be null");
        }
        if (statusUrl == null || statusUrl.isBlank()) {
            throw new IllegalArgumentException("ReplenishmentOrderResponseDTO: 'statusUrl' cannot be null or blank");
        }
    }
}
