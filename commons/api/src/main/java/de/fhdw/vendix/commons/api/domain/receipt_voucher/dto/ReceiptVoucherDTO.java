package de.fhdw.vendix.commons.api.domain.receipt_voucher.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.time.Instant;
import java.util.UUID;

public record ReceiptVoucherDTO(
        UUID code,
        boolean isRedeemed,
        Instant redeemedAt,
        Instant expiresAt
) implements DomainDTO {
    public ReceiptVoucherDTO {
        if (code == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'code' cannot be null");
        }
        if (redeemedAt == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'redeemedAt' cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'expiresAt' cannot be null");
        }
    }
}