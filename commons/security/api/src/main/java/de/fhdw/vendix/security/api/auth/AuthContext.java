package de.fhdw.vendix.security.api.auth;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import org.jspecify.annotations.Nullable;

public interface AuthContext {
    // Core
    AccountDTO account();
    // Meta
    @Nullable StoreDTO store();
    @Nullable RegisterDTO register();
    // Meta-Security
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
}