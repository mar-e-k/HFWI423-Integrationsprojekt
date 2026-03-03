package de.fhdw.vendix.commons.api.domain.store_stock.dto;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

public record StoreStockDTO(
    StoreDTO storeDTO,
    ArticleDTO articleDTO,
    long currentAmount,
    long criticalAmount,
    boolean isActive
) implements DomainDTO {
    public StoreStockDTO {
        if (storeDTO == null) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'storeDTO' cannot be null");
        }
        if (articleDTO == null) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'articleDTO' cannot be null");
        }
        if (currentAmount < 0) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'currentAmount' cannot be negative");
        }
        if (criticalAmount < 0) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'criticalAmount' cannot be negative");
        }
    }
}