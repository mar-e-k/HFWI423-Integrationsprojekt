package de.fhdw.vendix.commons.core.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;

import java.nio.charset.StandardCharsets;

// TODO: extract print methods from ReceiptService

public final class ReceiptPrinter {

    private ReceiptPrinter() {}

    public static byte[] printReceipt(ReceiptDTO receipt) {
        if (receipt == null) {
            throw new IllegalArgumentException("Parameter 'receipt' cannot be null");
        }
        if (receipt.lines() == null || receipt.lines().isEmpty()) {
            throw new IllegalArgumentException("Parameter 'receipt' must contain at least one item/line");
        }
        return "".getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] printVoucherReceipt(ReceiptDTO receipt) {
        if (receipt == null) {
            throw new IllegalArgumentException("Parameter 'receipt' cannot be null");
        }
        if (receipt.lines() == null || receipt.lines().isEmpty()) {
            throw new IllegalArgumentException("Parameter 'receipt' must contain at least one item/line");
        }
        if (receipt.vouchers() == null || receipt.vouchers().isEmpty()) {
            throw new IllegalArgumentException("Parameter 'receipt' must contain at least one voucher");
        }
        return "".getBytes(StandardCharsets.UTF_8);
    }
}