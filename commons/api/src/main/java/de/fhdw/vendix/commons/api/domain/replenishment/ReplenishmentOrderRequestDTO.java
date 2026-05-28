package de.fhdw.vendix.commons.api.domain.replenishment;

import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

public record ReplenishmentOrderRequestDTO(
        Long storeId,
        Long articleId,
        Long amount,
        Boolean urgent
) implements RequestDTO {

    public ReplenishmentOrderRequestDTO {
        if (storeId == null || storeId < 1) {
            throw new IllegalArgumentException("ReplenishmentOrderRequestDTO: 'storeId' must be >= 1");
        }
        if (articleId == null || articleId < 1) {
            throw new IllegalArgumentException("ReplenishmentOrderRequestDTO: 'articleId' must be >= 1");
        }
        if (amount == null || amount < 1) {
            throw new IllegalArgumentException("ReplenishmentOrderRequestDTO: 'amount' must be >= 1");
        }
        urgent = urgent != null && urgent;
    }
}
