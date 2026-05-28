package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

import java.util.List;
import java.util.UUID;

public record ReceiptRequestDTO(
        Long storeId,
        Long registerId,
        UUID cashierUuid,
        List<ReceiptLineDTO> lines,
        List<VoucherDTO> vouchers
) implements RequestDTO {
    public ReceiptRequestDTO {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'store' must not be null");
        }
        if (registerId == null || registerId <= 0) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'register' must not be null");
        }
        if (cashierUuid == null) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'cashierUuid' cannot be null or negative");
        }
        if (lines == null) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'lines' cannot be null");
        }
        if (vouchers == null) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'vouchers' cannot be null");
        }
    }
}