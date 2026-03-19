package de.fhdw.vendix.commons.api.domain.receipt_line.dto;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

import java.math.BigDecimal;

public record PriceOverrideDTO(
        BigDecimal price,
        OverrideReasonEnum reason
) implements EmbeddableDTO {
    public PriceOverrideDTO {
        if (price == null) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'price' cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'price' cannot be negative");
        }
        if (reason == null) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'reason' cannot be null");
        }
    }
}