package de.fhdw.vendix.commons.api.domain.account.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class AccountEndpoints implements WebEndpoint {

    public static final String BASE = "/api/account";

    public static final String BY_ID = BASE + "/id/{id}";
    public static final String BY_UUID = BASE + "/uuid/{uuid}";
    public static final String BY_USERNAME = BASE + "/name/{username}";

    private AccountEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of(
                BY_ID,
                BY_UUID,
                BY_USERNAME
        );
    }
}