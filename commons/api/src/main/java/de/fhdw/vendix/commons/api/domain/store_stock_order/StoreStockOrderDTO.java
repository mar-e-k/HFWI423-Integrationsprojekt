package de.fhdw.vendix.commons.api.domain.store_stock_order;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record StoreStockOrderDTO(
        @Nullable Long id,
        UUID correlationId,
        Long storeId,
        Long articleId,
        Long amount,
        Boolean urgent,
        OrderStatus status,
        @Nullable String message,
        Instant createdAt,
        Instant updatedAt
) implements DomainDTO {

    public StoreStockOrderDTO {
        if (id != null && id <= 0L) {
            throw new IllegalArgumentException("StoreStockOrderDTO: 'id' must be greater than or equal to zero");
        }
        if (correlationId == null) {
            throw new IllegalArgumentException("StoreStockOrderDTO: 'correlationId' cannot be null");
        }
        if (storeId == null || storeId < 1) {
            throw new IllegalArgumentException("StoreStockOrderDTO: 'storeId' must be >= 1");
        }
        if (articleId == null || articleId < 1) {
            throw new IllegalArgumentException("StoreStockOrderDTO: 'articleId' must be >= 1");
        }
        if (amount == null || amount < 1) {
            throw new IllegalArgumentException("StoreStockOrderDTO: 'amount' must be >= 1");
        }
        urgent = urgent != null && urgent;
        if (status == null) {
            throw new IllegalArgumentException("StoreStockOrderDTO: 'status' cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("StoreStockOrderDTO: 'createdAt' cannot be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("StoreStockOrderDTO: 'updatedAt' cannot be null");
        }
    }
}
