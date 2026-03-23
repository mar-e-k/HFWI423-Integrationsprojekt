package de.fhdw.vendix.commons.api.domain.account.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

public final class AccountEndpoints implements WebEndpoint {

    public static final String BASE = "/api/account";
    public static final String BY_ID = BASE + "/id/{id}";
    public static final String BY_UUID = BASE + "/uuid/{uuid}";
    public static final String BY_USERNAME = BASE + "/name/{username}";

    private AccountEndpoints() {}
}