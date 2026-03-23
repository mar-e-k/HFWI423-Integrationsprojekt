package de.fhdw.vendix.commons.api.domain.receipt_voucher.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class ReceiptVoucherEndpoints implements WebEndpoint {

    public static final String BASE = "/api/receipt/voucher/";

    public static final String BY_CODE = BASE + "redeem/{code}";

    private ReceiptVoucherEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of(
                BY_CODE
        );
    }
}