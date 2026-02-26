package de.fhdw.vendix.commons.core.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;

import java.nio.charset.StandardCharsets;

// TODO: extract print methods from ReceiptService

public final class ReceiptPrinter {

    private ReceiptPrinter() {}

    public static byte[] printReceipt(ReceiptDTO receipt) {
        return "".getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] printDailyReceipt(ReceiptDTO receipt) {
        return "".getBytes(StandardCharsets.UTF_8);
    }
}