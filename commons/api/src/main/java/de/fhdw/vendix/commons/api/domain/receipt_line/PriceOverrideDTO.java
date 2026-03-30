package de.fhdw.vendix.commons.api.domain.receipt_line;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

import java.math.BigDecimal;

public record PriceOverrideDTO(
        BigDecimal price,
        OverrideReason overrideReason
) implements EmbeddableDTO {
    public PriceOverrideDTO {
        if (price == null) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'price' cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'price' cannot be negative");
        }
        if (overrideReason == null) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'overrideReason' cannot be null");
        }
    }
}