package de.fhdw.vendix.commons.core.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;

// TODO: extract print methods from ReceiptService

public final class ReceiptPrinter {

    private ReceiptPrinter() {}

    public static byte[] printReceipt(ReceiptDTO receipt) {
        return "".getBytes();
    }

    public static byte[] printDailyReceipt(ReceiptDTO receipt) {
        return "".getBytes();
    }
}