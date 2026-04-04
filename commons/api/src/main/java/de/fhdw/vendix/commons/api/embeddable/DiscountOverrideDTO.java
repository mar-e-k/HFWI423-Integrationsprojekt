package de.fhdw.vendix.commons.api.embeddable;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

import java.math.BigDecimal;

public record DiscountOverrideDTO(
        BigDecimal amount,
        OverrideReason reason
) implements EmbeddableDTO {
    public DiscountOverrideDTO {
        if (amount == null) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'amount' cannot be null");
        }
        if (amount.compareTo(BigDecimal.valueOf(0)) < 0 || amount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'amount' must be between 0 and 100");
        }
        if (reason == null) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'reason' cannot be null");
        }
    }
}
