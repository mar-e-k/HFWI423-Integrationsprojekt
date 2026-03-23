package de.fhdw.vendix.commons.api.domain.register.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class RegisterEndpoints implements WebEndpoint {

    public static final String BASE = "/api/register";

    private RegisterEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of();
    }
}
