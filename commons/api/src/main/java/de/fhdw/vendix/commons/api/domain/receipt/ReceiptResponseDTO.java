package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

import java.util.UUID;

public record ReceiptResponseDTO(
        Long id,
        Long storeId,
        Long registerId,
        UUID cashierUuid,
        PaymentMethod paymentMethod,
        ReceiptStatus status
) implements ResponseDTO {
    public ReceiptResponseDTO {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("ReceiptResponseDTO parameter 'id' must be at least 0");
        }
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException("ReceiptResponseDTO parameter 'store' must not be null");
        }
        if (registerId == null || registerId <= 0) {
            throw new IllegalArgumentException("ReceiptResponseDTO parameter 'register' must not be null");
        }
        if (cashierUuid == null) {
            throw new IllegalArgumentException("ReceiptResponseDTO parameter 'cashierUuid' cannot be null or negative");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("ReceiptResponseDTO parameter 'paymentMethod' must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("ReceiptResponseDTO parameter 'status' must not be null");
        }
    }
}