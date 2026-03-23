package de.fhdw.vendix.commons.api.domain.receipt.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class ReceiptEndpoints implements WebEndpoint {

    public static final String BASE = "/api/receipt";

    private ReceiptEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of();
    }
}