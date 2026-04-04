package de.fhdw.vendix.store.core.domain.receipt_voucher;

public class VoucherExpiredException extends RuntimeException {
    public VoucherExpiredException(String message) {
        super(message);
    }
}
