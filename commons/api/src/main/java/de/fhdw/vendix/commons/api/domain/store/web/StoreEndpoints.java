package de.fhdw.vendix.commons.api.domain.store.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class StoreEndpoints implements WebEndpoint {

    public static final String BASE = "/api/store";

    private StoreEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of();
    }
}