package de.fhdw.vendix.commons.security.jwt.claims;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;

import java.util.Set;
import java.util.UUID;

public record AuthClaims(
        String accountUuid,
        Set<AccountRoleEnum> accountRoles
) {
    public AuthClaims {
        if (accountUuid == null || accountUuid.isEmpty()) {
            throw new IllegalArgumentException("Account uuid cannot be null or empty");
        }
        if (accountRoles == null || accountRoles.isEmpty()) {
            throw new IllegalArgumentException("Account roles cannot be null or empty");
        }
    }

    public static AuthClaims system() {
        return new AuthClaims(
                UUID.randomUUID().toString(), // TODO: change to actual application instance via a spring bean or similar
                Set.of(AccountRoleEnum.SYSTEM)
        );
    }

    public static AuthClaims user(String subject, Set<AccountRoleEnum> roles) {
        return new AuthClaims(
                subject,
                Set.copyOf(roles)
        );
    }
}