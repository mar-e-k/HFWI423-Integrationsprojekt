package de.fhdw.vendix.store.core.domain.receipt;

public class ReceiptAlreadyPrintedException extends Exception {
    public ReceiptAlreadyPrintedException(String message) {
        super(message);
    }
}