package de.fhdw.vendix.commons.api.domain.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record ReceiptVoucherDTO(
        @Nullable Long id,
        ReceiptDTO receipt,
        UUID code,
        @Nullable Instant expiresAt,
        @Nullable Instant redeemedAt
) implements DomainDTO<Long> {
    public ReceiptVoucherDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'id' cannot be negative");
        }
        if (receipt == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'receipt' cannot be null");
        }
        if (code == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'code' cannot be null");
        }
    }

    @Override
    public @Nullable Long getIdentifiable() {
        return id;
    }
}