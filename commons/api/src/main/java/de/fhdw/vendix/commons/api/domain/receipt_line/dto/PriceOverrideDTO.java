package de.fhdw.vendix.commons.api.domain.receipt_line.dto;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

import java.math.BigDecimal;

public record PriceOverrideDTO(
        BigDecimal overriddenPrice,
        OverrideReasonEnum overriddenPriceReason
) implements EmbeddableDTO {
    public PriceOverrideDTO {
        if (overriddenPrice == null) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'overriddenPrice' cannot be null");
        }
        if (overriddenPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'overriddenPrice' cannot be negative");
        }
        if (overriddenPriceReason == null) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'overriddenPriceReason' cannot be null");
        }
    }
}