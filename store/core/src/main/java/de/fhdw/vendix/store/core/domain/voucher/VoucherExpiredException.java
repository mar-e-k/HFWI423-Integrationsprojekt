package de.fhdw.vendix.store.core.domain.voucher;

public class VoucherExpiredException extends RuntimeException {
    public VoucherExpiredException(String message) {
        super(message);
    }
}
