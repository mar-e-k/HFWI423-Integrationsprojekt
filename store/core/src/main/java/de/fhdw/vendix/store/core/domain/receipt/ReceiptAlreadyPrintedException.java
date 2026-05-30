package de.fhdw.vendix.store.core.domain.receipt;

public class ReceiptAlreadyPrintedException extends RuntimeException {
    public ReceiptAlreadyPrintedException(String message) {
        super(message);
    }
}