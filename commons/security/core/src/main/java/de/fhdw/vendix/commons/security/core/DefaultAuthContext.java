package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.security.api.auth.AuthContext;
import org.jspecify.annotations.Nullable;

public record DefaultAuthContext(
        // Core
        AccountDTO account,
        // Meta-Security
        boolean isAccountNonExpired,
        boolean isAccountNonLocked,
        boolean isCredentialsNonExpired,
        boolean isEnabled
) implements AuthContext {
    public DefaultAuthContext {
        if (account == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'cashier' cannot be null");
        }
    }

    public DefaultAuthContext(AccountDTO account) {
        this(
                account,
                true,
                true,
                true,
                true
        );
    }
}