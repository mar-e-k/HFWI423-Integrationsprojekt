package de.fhdw.vendix.commons.api.domain.article.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public record ArticleDTO(
        @Nullable Long id,
        long gtin,
        String name,
        String description,
        String manufacturer,
        String supplier,
        String unit,
        BigDecimal purchasePrice,
        BigDecimal sellingPrice,
        BigDecimal taxRate,
        long stock,
        boolean isAvailable,
        boolean isDeposit
) implements DomainDTO<Long> {
    public ArticleDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'id' cannot be negative");
        }
        if (gtin < 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'gtin' cannot be negative");
        }
        if (List.of(8, 12, 13, 14).contains(String.valueOf(gtin).length())) {
            throw new IllegalArgumentException("ArticleDTO parameter 'gtin' must be 8, 12, 13, 14 lengths long");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ArticleDTO parameter 'name' cannot be null or empty");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("ArticleDTO parameter 'description' cannot be null or empty");
        }
        if (manufacturer == null || manufacturer.isEmpty()) {
            throw new IllegalArgumentException("ArticleDTO parameter 'manufacturer' cannot be null or empty");
        }
        if (supplier == null || supplier.isEmpty()) {
            throw new IllegalArgumentException("ArticleDTO parameter 'supplier' cannot be null or empty");
        }
        if (unit == null || unit.isEmpty()) {
            throw new IllegalArgumentException("ArticleDTO parameter 'unit' cannot be null or empty");
        }
        if (purchasePrice == null) {
            throw new IllegalArgumentException("ArticleDTO parameter 'purchasePrice' cannot be null");
        }
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'purchasePrice' must be greater than or equal to 0");
        }
        if (sellingPrice == null) {
            throw new IllegalArgumentException("ArticleDTO parameter 'sellingPrice' cannot be null");
        }
        if (sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'sellingPrice' must be greater than or equal to 0");
        }
        if (taxRate == null) {
            throw new IllegalArgumentException("ArticleDTO parameter 'taxRate' cannot be null");
        }
        if (taxRate.compareTo(BigDecimal.valueOf(0)) < 1 || taxRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'taxRate' must be between 0 and 100");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'stock' cannot be negative");
        }
    }

    @Override
    public @Nullable Long getIdentifiable() {
        return id;
    }
}