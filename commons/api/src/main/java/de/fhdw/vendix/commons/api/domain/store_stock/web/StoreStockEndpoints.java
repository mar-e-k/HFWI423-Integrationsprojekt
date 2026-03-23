package de.fhdw.vendix.commons.api.domain.store_stock.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class StoreStockEndpoints implements WebEndpoint {

    public static final String BASE = "/api/store/stock";

    private StoreStockEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of();
    }
}