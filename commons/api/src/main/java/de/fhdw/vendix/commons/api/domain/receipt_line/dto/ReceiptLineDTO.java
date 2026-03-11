package de.fhdw.vendix.commons.api.domain.receipt_line.dto;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record ReceiptLineDTO(
        @Nullable Long id,
        ReceiptDTO receipt,
        ArticleDTO article,
        long amount,
        @Nullable PriceOverrideDTO priceOverride,
        @Nullable DiscountOverrideDTO discountOverride
) implements DomainDTO {
    public ReceiptLineDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'id' cannot be negative");
        }
        if (receipt == null) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'receipt' cannot be null");
        }
        if (article == null) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'article' cannot be null");
        }
        if (amount < 1) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'amount' must be greater than or equal to 1");
        }
    }
}