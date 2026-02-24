package de.fhdw.vendix.commons.api.domain.receipt.dto.line;

import de.fhdw.vendix.commons.api.structure.dto.DTO;

public record DiscountOverrideDTO(
        Float overriddenDiscount,
        OverrideReason overriddenDiscountReason
) implements DTO {
    public DiscountOverrideDTO {
        if (overriddenDiscount == null) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'overriddenDiscount' cannot be null");
        }
        if (overriddenDiscount < 0 || overriddenDiscount > 100) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'overriddenDiscount' must be between 0 and 100");
        }
        if (overriddenDiscountReason == null) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'overriddenDiscountReason' cannot be null");
        }
    }
}
