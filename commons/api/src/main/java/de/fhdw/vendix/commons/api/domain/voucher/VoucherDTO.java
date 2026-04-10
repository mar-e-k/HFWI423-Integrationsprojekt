package de.fhdw.vendix.commons.api.domain.voucher;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record VoucherDTO(
        @Nullable Long id,
        Long receiptId,
        UUID code,
        @Nullable Instant expiresAt,
        @Nullable Instant redeemedAt
) implements DomainDTO {
    public VoucherDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("VoucherDTO parameter 'id' cannot be negative");
        }
        if (receiptId == null || receiptId < 0) {
            throw new IllegalArgumentException("VoucherDTO parameter 'receipt' cannot be null or negative");
        }
        if (code == null) {
            throw new IllegalArgumentException("VoucherDTO parameter 'code' cannot be null");
        }
    }
}