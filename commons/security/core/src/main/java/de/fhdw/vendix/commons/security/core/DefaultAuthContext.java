package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.security.api.context.AuthContext;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public record DefaultAuthContext(
        // Core
        @Nullable Long accountId,
        UUID accountUUID,
        String accountUsername,
        String accountPassword,
        Set<AccountRoleEnum> accountRoles
) implements AuthContext {
    public DefaultAuthContext {
        if (accountId == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'accountId' cannot be null.");
        }
        if (accountUUID == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'accountUUID' cannot be null.");
        }
        if (accountUsername == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'accountUsername' cannot be null.");
        }
        if (accountPassword == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'accountPassword' cannot be null.");
        }
        if (accountRoles == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'accountRoles' cannot be null.");
        }
    }

    public DefaultAuthContext(
            AccountDTO account,
            Set<AccountRoleEnum> roles
    ) {
        this(
                account.id(),
                account.uuid(),
                account.username(),
                account.password(),
                Collections.unmodifiableSet(roles)
        );
    }
}