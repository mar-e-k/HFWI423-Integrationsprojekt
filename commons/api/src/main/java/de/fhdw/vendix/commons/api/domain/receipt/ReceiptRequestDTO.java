package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

import java.util.List;

public record ReceiptRequestDTO(
        StoreDTO store,
        RegisterDTO register,
        Long accountId,
        List<ReceiptLineDTO> lines,
        List<ReceiptVoucherDTO> vouchers
) implements RequestDTO {
    public ReceiptRequestDTO {
        if (store == null) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'store' must not be null");
        }
        if (register == null) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'register' must not be null");
        }
        if (accountId == null || accountId < 0) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'cashier' cannot be null or negative");
        }
        if (lines == null) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'lines' cannot be null");
        }
        if (vouchers == null) {
            throw new IllegalArgumentException("ReceiptRequestDTO parameter 'vouchers' cannot be null");
        }
    }
}