package de.fhdw.vendix.commons.spring.security.context.auth;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.Role;

import java.util.Set;

public record DefaultAuthContext(
        AccountDTO account,
        Set<Role> roles
) implements AuthContext {
    public DefaultAuthContext {
        if (account == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'account' cannot be null.");
        }
        if (roles == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'roles' cannot be null.");
        }
    }
}