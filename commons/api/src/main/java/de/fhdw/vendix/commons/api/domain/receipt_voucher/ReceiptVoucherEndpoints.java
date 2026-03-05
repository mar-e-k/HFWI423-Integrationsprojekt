package de.fhdw.vendix.commons.api.domain.receipt_voucher;

public final class ReceiptVoucherEndpoints {

    public static final String BASE = "/api/receipt/voucher";

    public static final String BY_CODE = BASE + "redeem/{code}";

    private ReceiptVoucherEndpoints() {}
}