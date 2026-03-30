package de.fhdw.vendix.security.api.jwt.payload.claims;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;

import java.util.Set;
import java.util.UUID;

public record AuthClaims(
        UUID subject,
        Set<AccountRole> accountRoles
) {
    public AuthClaims {
        if (subject == null) {
            throw new IllegalArgumentException("AuthClaims parameter 'subject' cannot be null");
        }
        if (accountRoles == null) {
            throw new IllegalArgumentException("AuthClaims parameter 'subject' cannot be null");
        }
    }
}