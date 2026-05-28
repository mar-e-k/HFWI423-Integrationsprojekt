package de.fhdw.vendix.commons.api.domain.store_stock;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.embeddable.PreferenceAmountDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record StoreStockDTO(
        @Nullable Long id,
        Long storeId,
        ArticleDTO article,
        PreferenceAmountDTO preferenceAmount,
        Long currentAmount
) implements DomainDTO {
    public StoreStockDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'id' cannot be negative");
        }
        if (storeId == null || storeId < 0) {
            throw new IllegalArgumentException("StoreStockDTO parameter 'storeId' cannot be null or negative");
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