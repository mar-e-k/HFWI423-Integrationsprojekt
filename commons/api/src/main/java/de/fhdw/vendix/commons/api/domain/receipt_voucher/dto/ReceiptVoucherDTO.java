package de.fhdw.vendix.commons.api.domain.receipt_voucher.dto;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.time.Instant;
import java.util.UUID;

public record ReceiptVoucherDTO(
        ReceiptDTO receipt,
        UUID code,
        Instant expiresAt,
        Instant redeemedAt
) implements DomainDTO {
    public ReceiptVoucherDTO {
        if (receipt == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'receipt' must not be null");
        }
        if (code == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'code' cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'expiresAt' cannot be null");
        }
        if (redeemedAt == null) {
            throw new IllegalArgumentException("ReceiptVoucherDTO parameter 'redeemedAt' cannot be null");
        }
    }
}