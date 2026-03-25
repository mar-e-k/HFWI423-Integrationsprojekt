package de.fhdw.vendix.commons.api.domain.account.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class AccountEndpoints implements WebEndpoint {

    public static final String BASE = "/api/account";

    public static final String BY_ID = BASE + "/id/{id}";
    public static final String BY_UUID = BASE + "/uuid/{uuid}";
    public static final String BY_USERNAME = BASE + "/username/{username}";
    public static final String BY_PHONE = BASE + "/phone/{phone}";
    public static final String BY_EMAIL = BASE + "/email/{email}";
    public static final String ROLE = BASE + "/id/{id}/role";

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
                BY_USERNAME,
                BY_PHONE,
                BY_EMAIL,
                ROLE
        );
    }
}