package de.fhdw.vendix.commons.api.domain.article;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

public record ArticleDTO(
        @Nullable Long id,
        String gtin,
        String name,
        String description,
        String manufacturer,
        String supplier,
        String unit,
        BigDecimal purchasePrice,
        BigDecimal sellingPrice,
        BigDecimal taxRate,
        Long stock,
        Boolean isAvailable,
        Boolean isDeposit
) implements DomainDTO {
    public ArticleDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'id' cannot be negative");
        }
        if (gtin == null || gtin.isBlank() || !gtin.matches("^\\d{8}(\\d{4}|\\d{5}|\\d{6})?$")) {
            throw new IllegalArgumentException("ArticleDTO parameter 'gtin' cannot be null or blank and must follow the gtin format (8,12,13 or 14 digits)");
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
        if (taxRate.compareTo(BigDecimal.ZERO) < 0 || taxRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'taxRate' must be between 0 and 100");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("ArticleDTO parameter 'stock' cannot be null or negative");
        }
        if (isAvailable == null) {
            throw new IllegalArgumentException("ArticleDTO parameter 'isAvailable' cannot be negative");
        }
        if (isDeposit == null) {
            throw new IllegalArgumentException("ArticleDTO parameter 'isDeposit' cannot be negative");
        }
    }
}