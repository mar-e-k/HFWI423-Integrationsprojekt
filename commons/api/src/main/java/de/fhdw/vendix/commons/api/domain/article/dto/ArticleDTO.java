package de.fhdw.vendix.commons.api.domain.article.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.math.BigDecimal;
import java.util.List;

public record ArticleDTO(
        long articleId,
        long gtin,
        String name,
        String description,
        BigDecimal purchasePrice,
        BigDecimal sellingPrice,
        float taxRate
) implements DomainDTO {

    public ArticleDTO {
        if (articleId < 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'articleId' must be greater than or equal to 0");
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
        if (taxRate < 0 || taxRate > 100) {
            throw new IllegalArgumentException("ArticleDTO parameter 'taxRate' must be between 0 and 100");
        }
    }
}