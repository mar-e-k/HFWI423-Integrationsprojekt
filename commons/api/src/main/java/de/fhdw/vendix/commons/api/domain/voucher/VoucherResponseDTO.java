package de.fhdw.vendix.commons.api.domain.voucher;

import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record VoucherResponseDTO(
        Long id,
        UUID code,
        @Nullable Instant expiresAt,
        @Nullable Instant redeemedAt
) implements ResponseDTO {
    public VoucherResponseDTO {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("VoucherResponseDTO parameter 'id' cannot be negative");
        }
        if (code == null) {
            throw new IllegalArgumentException("VoucherResponseDTO parameter 'code' cannot be null");
        }
    }
}