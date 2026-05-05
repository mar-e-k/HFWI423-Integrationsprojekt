package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record ReceiptDTO(
        @Nullable Long id,
        Long storeId,
        Long registerId,
        Long cashierId,
        ReceiptPaymentMethod receiptPaymentMethod,
        ReceiptStatus status
) implements DomainDTO {
    public ReceiptDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'id' must be at least 0");
        }
        if (storeId == null || storeId < 0) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'storeId' cannot be null or negative");
        }
        if (registerId == null || registerId < 0) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'registerId' cannot be null or negative");
        }
        if (cashierId == null || cashierId < 0) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'cashierId' must not be null or negative");
        }
        if (receiptPaymentMethod == null) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'paymentMethod' must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'status' must not be null");
        }
    }
}