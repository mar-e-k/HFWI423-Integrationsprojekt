package de.fhdw.vendix.commons.api.domain.receipt.dto;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.util.List;

public record ReceiptDTO (
        long receiptId,
        long storeId,
        long registerId,
        long accountId,
        List<ReceiptLineDTO> lines
) implements DomainDTO {
    public ReceiptDTO {
        if (receiptId < 0) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'receiptId' must be at least 0");
        }
        if (storeId < 0) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'storeId' must be at least 0");
        }
        if (registerId < 0) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'registerId' must be at least 0");
        }
        if (accountId < 0) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'accountId' must be at least 0");
        }
        if (lines == null) {
            throw new IllegalArgumentException("ReceiptLineDTO parameter 'lines' cannot be not null");
        }
    }
}