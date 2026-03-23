package de.fhdw.vendix.commons.api.domain.account_role.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class AccountRoleEndpoints implements WebEndpoint {

    private static final String BASE = "/api/account/role";

    private AccountRoleEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of();
    }
}