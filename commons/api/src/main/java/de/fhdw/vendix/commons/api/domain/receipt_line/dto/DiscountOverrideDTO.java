package de.fhdw.vendix.commons.api.domain.receipt_line.dto;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

import java.math.BigDecimal;

public record DiscountOverrideDTO(
        BigDecimal discount,
        OverrideReasonEnum reason
) implements EmbeddableDTO {
    public DiscountOverrideDTO {
        if (discount == null) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'discount' cannot be null");
        }
        if (discount.compareTo(BigDecimal.valueOf(0)) < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'discount' must be between 0 and 100");
        }
        if (reason == null) {
            throw new IllegalArgumentException("DiscountOverrideDTO parameter 'reason' cannot be null");
        }
    }
}
