package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.security.api.auth.AuthContext;
import org.jspecify.annotations.Nullable;

public record DefaultAuthContext (
        // Core
        AccountDTO account,
        // Meta
        @Nullable StoreDTO store,
        @Nullable RegisterDTO register,
        // Meta-Security
        boolean isAccountNonExpired,
        boolean isAccountNonLocked,
        boolean isCredentialsNonExpired,
        boolean isEnabled
) implements AuthContext {
    public DefaultAuthContext {
        if (account == null) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'account' cannot be null");
        }
        if (store != null && register != null && store.storeId() != register.storeId()) {
            throw new IllegalArgumentException("DefaultAuthContext parameter 'store' and 'register' cannot have differing store id's");
        }
    }

    public DefaultAuthContext(AccountDTO account, @Nullable StoreDTO store, @Nullable RegisterDTO register) {
        this(account, store, register, true, true, true, true);
    }

    public DefaultAuthContext(AccountDTO account) {
        this(account, null, null);
    }
}