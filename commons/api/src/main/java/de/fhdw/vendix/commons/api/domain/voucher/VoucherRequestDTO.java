package de.fhdw.vendix.commons.api.domain.voucher;

import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record VoucherRequestDTO(
        UUID code,
        @Nullable Instant expiresAt
) implements RequestDTO {
    public VoucherRequestDTO {
        if (code == null) {
            throw new IllegalArgumentException("VoucherRequestDTO parameter 'code' cannot be null");
        }
    }
}