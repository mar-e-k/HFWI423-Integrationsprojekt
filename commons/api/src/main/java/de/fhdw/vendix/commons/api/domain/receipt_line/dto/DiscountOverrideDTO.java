package de.fhdw.vendix.commons.api.domain.receipt_line.dto;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

import java.math.BigDecimal;

public record DiscountOverrideDTO(
        BigDecimal overriddenDiscount,
        OverrideReasonEnum overriddenDiscountReason
) implements EmbeddableDTO {
    public DiscountOverrideDTO {
        if (overriddenDiscount == null) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'overriddenDiscount' cannot be null");
        }
        if (overriddenDiscount.compareTo(BigDecimal.valueOf(0)) < 0 || overriddenDiscount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'overriddenDiscount' must be between 0 and 100");
        }
        if (overriddenDiscountReason == null) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'overriddenDiscountReason' cannot be null");
        }
    }
}
