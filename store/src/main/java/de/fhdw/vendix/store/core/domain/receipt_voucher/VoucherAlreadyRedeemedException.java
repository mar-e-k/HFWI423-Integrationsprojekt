package de.fhdw.vendix.store.core.domain.receipt_voucher;

public class VoucherAlreadyRedeemedException extends RuntimeException {
    public VoucherAlreadyRedeemedException(String message) {
        super(message);
    }
}
