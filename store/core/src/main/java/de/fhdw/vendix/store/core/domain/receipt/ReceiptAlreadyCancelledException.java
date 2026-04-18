package de.fhdw.vendix.store.core.domain.receipt;

public class ReceiptAlreadyCancelledException extends Exception {
    public ReceiptAlreadyCancelledException(String message) {
        super(message);
    }
}