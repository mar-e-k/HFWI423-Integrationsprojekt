package de.fhdw.vendix.store.core.domain.receipt;

public class ReceiptAlreadyCancelledException extends RuntimeException {
    public ReceiptAlreadyCancelledException(String message) {
        super(message);
    }
}