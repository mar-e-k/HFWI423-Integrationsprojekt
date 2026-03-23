package de.fhdw.vendix.commons.api.domain.receipt_line.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class ReceiptLineEndpoints implements WebEndpoint {

    public static final String BASE = "/api/receipt/line";

    private ReceiptLineEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of();
    }
}