package de.fhdw.vendix.commons.api.domain.replenishment;

import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record ReplenishmentOrderStatusDTO(
        UUID correlationId,
        Long storeId,
        Long articleId,
        Long amount,
        Boolean urgent,
        ReplenishmentOrderStatus status,
        @Nullable String message,
        Instant createdAt,
        Instant updatedAt
) implements ResponseDTO {

    public ReplenishmentOrderStatusDTO {
        if (correlationId == null) {
            throw new IllegalArgumentException("ReplenishmentOrderStatusDTO: 'correlationId' cannot be null");
        }
        if (storeId == null || storeId < 1) {
            throw new IllegalArgumentException("ReplenishmentOrderStatusDTO: 'storeId' must be >= 1");
        }
        if (articleId == null || articleId < 1) {
            throw new IllegalArgumentException("ReplenishmentOrderStatusDTO: 'articleId' must be >= 1");
        }
        if (amount == null || amount < 1) {
            throw new IllegalArgumentException("ReplenishmentOrderStatusDTO: 'amount' must be >= 1");
        }
        urgent = urgent != null && urgent;
        if (status == null) {
            throw new IllegalArgumentException("ReplenishmentOrderStatusDTO: 'status' cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("ReplenishmentOrderStatusDTO: 'createdAt' cannot be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("ReplenishmentOrderStatusDTO: 'updatedAt' cannot be null");
        }
    }
}
