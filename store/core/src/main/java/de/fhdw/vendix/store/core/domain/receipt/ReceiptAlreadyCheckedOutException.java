package de.fhdw.vendix.store.core.domain.receipt;

public class ReceiptAlreadyCheckedOutException extends RuntimeException {
    public ReceiptAlreadyCheckedOutException(String message) {
        super(message);
    }
}
