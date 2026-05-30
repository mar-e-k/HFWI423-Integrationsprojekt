package de.fhdw.vendix.commons.api.domain.store_stock_order;

import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

public record StoreStockOrderRequestDTO(
        Long storeId,
        Long articleId,
        Long amount,
        Boolean urgent
) implements RequestDTO {

    public StoreStockOrderRequestDTO {
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