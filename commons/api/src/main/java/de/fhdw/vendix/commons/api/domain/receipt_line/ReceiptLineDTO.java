package de.fhdw.vendix.commons.api.domain.receipt_line;

import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.embeddable.PriceOverrideDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record ReceiptLineDTO(
        @Nullable Long id,
        Long receiptId,
        Long articleId,
        Long articleAmount,
        @Nullable DiscountOverrideDTO discountOverride,
        @Nullable PriceOverrideDTO priceOverride
) implements DomainDTO {
    public ReceiptLineDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'id' cannot be negative or negative");
        }
        if (receiptId == null || receiptId < 0) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'receipt' cannot be null or negative");
        }
        if (articleId == null || articleId < 0) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'article' cannot be null or negative");
        }
        if (articleAmount == null || articleAmount < 1) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'articleAmount' cannot be null and must be greater than or equal to 1");
        }
    }
}