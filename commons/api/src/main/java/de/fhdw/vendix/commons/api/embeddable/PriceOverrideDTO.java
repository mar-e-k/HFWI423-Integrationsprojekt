package de.fhdw.vendix.commons.api.embeddable;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

import java.math.BigDecimal;

public record PriceOverrideDTO(
        BigDecimal amount,
        OverrideReason reason
) implements EmbeddableDTO {
    public PriceOverrideDTO {
        if (amount == null) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'articleAmount' cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'articleAmount' cannot be negative");
        }
        if (reason == null) {
            throw new IllegalArgumentException("PriceOverrideDTO parameter 'reason' cannot be null");
        }
    }
}