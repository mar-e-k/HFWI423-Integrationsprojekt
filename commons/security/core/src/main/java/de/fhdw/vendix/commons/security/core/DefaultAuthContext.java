package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.security.api.authentication.AuthContext;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record DefaultAuthContext(
        // Core
        Long accountID,
        UUID accountUUID,
        String accountUsername,
        String accountPassword,
        Set<AccountRoleEnum> accountRoles,
        // Meta-Security
        boolean isAccountNonExpired,
        boolean isAccountNonLocked,
        boolean isCredentialsNonExpired,
        boolean isEnabled
) implements AuthContext {
    public DefaultAuthContext {
        if (accountID == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'accountID' cannot be null.");
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
            Long accountID,
            UUID accountUUID,
            String accountUsername,
            String accountPassword,
            Set<AccountRoleEnum> accountRoles
    ) {
        this(
                accountID,
                accountUUID,
                accountUsername,
                accountPassword,
                Collections.unmodifiableSet(accountRoles),
                true,
                true,
                true,
                true
        );
    }

    public DefaultAuthContext(
            AccountDTO account,
            Set<AccountRoleEnum> roles,
            boolean isAccountNonExpired,
            boolean isAccountNonLocked,
            boolean isCredentialsNonExpired,
            boolean isEnabled
    ) {
        Objects.requireNonNull(account.id(), "DefaultAuthContext parameter 'accountID' cannot be null.");
        this(
                account.id(),
                account.uuid(),
                account.username(),
                account.password(),
                Collections.unmodifiableSet(roles),
                isAccountNonExpired,
                isAccountNonLocked,
                isCredentialsNonExpired,
                isEnabled
        );
    }

    public DefaultAuthContext(AccountDTO account, Set<AccountRoleEnum> roles) {
        Objects.requireNonNull(account.id(), "DefaultAuthContext parameter 'accountID' cannot be null.");
        this(
                account,
                roles,
                true,
                true,
                true,
                true
        );
    }
}