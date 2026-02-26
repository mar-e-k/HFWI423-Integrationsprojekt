package de.fhdw.vendix.commons.security.auth;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.role.dto.RoleDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@NullMarked
public record AuthContext(
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
) implements UserDetails, Principal {
    public AuthContext {
        if (account == null) {
            throw new IllegalArgumentException("AccountDTO parameter 'account' cannot be null");
        }
    }

    @Override
    public String getName() {
        return account.username();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return account.roles().stream()
                .map(RoleDTO::role)
                .map(Enum::name)
                .map("ROLE_"::concat)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return account.password();
    }

    @Override
    public String getUsername() {
        return account.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}