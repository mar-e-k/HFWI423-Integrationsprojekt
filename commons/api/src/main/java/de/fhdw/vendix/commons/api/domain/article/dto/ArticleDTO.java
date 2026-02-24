package de.fhdw.vendix.commons.api.domain.article.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.math.BigDecimal;

public record ArticleDTO(
        long articleId,
        String gtin,
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
        if (gtin == null) {
            throw new IllegalArgumentException("ArticleDTO parameter 'gtin' cannot be null");
        }
        if (!gtin.matches("^(?:\\d{8}|\\d{12}|\\d{13}|\\d{14})$")) {
            throw new IllegalArgumentException("ArticleDTO parameter 'gtin' must be a valid GTIN");
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