package de.fhdw.vendix.commons.api.domain.receipt.dto;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record ReceiptDTO (
        long receiptId,
        StoreDTO store,
        RegisterDTO register,
        AccountDTO account,
        @Nullable List<ReceiptLineDTO> lines,
        @Nullable List<ReceiptVoucherDTO> vouchers
) implements DomainDTO {
    public ReceiptDTO {
        if (receiptId < 0) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'receiptId' must be at least 0");
        }
        if (store == null) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'store' must not be null");
        }
        if (account == null) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'account' must not be null");
        }
    }
}