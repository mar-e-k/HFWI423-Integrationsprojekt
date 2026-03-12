package de.fhdw.vendix.commons.api.domain.store_stock.dto;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record StoreStockDTO(
        @Nullable Long id,
        StoreDTO store,
        ArticleDTO article,
        PreferenceAmountDTO preferenceAmount,
        long currentAmount,
        boolean isActive
) implements DomainDTO {
    public StoreStockDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'id' cannot be negative");
        }
        if (store == null) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'store' cannot be null");
        }
        if (article == null) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'article' cannot be null");
        }
        if (preferenceAmount == null) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'preferenceAmount' cannot be null");
        }
        if (currentAmount < 0) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'currentAmount' cannot be negative");
        }
    }
}