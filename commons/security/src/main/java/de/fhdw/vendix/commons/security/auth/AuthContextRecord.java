package de.fhdw.vendix.commons.security.auth;

import de.fhdw.vendix.commons.api.domain.role.dto.Role;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Maybe split up record fields into three components, where core is mandatory.
@NullMarked
public record AuthContextRecord(
        // Core
        long accountId,
        UUID uuid,
        String username,
        String password,
        Set<Role> roles,
        // Meta
        long storeId,
        long registerId,
        // Meta-Security
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean accountCredentialsNonExpired,
        boolean accountEnabled
) implements UserDetails, Principal {
    public AuthContextRecord {
        if (accountId < 0) {
            throw new IllegalArgumentException("AuthContext parameter 'accountId' cannot be negative");
        }
        if (uuid == null) {
            throw new IllegalArgumentException("AuthContext parameter 'uuid' cannot be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("AuthContext parameter 'username' cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("AuthContext parameter 'password' cannot be null");
        }
        if (roles == null) {
            throw new IllegalArgumentException("AuthContext parameter 'roles' cannot be null");
        }
        if (storeId < 0) {
            throw new IllegalArgumentException("AuthContext parameter 'storeId' cannot be negative");
        }
        if (registerId < 0) {
            throw new IllegalArgumentException("AuthContext parameter 'registerId' cannot be negative");
        }
    }

    public AuthContextRecord(
            long accountId,
            UUID uuid,
            String username,
            String password,
            Set<Role> roles,
            long storeId,
            long registerId
    ) {
        this(
                accountId,
                uuid,
                username,
                password,
                roles,
                storeId,
                registerId,
                true,
                true,
                false,
                true
        );
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        return roles.stream()
                .map(Enum::name)
                .map("ROLE_"::concat)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return accountCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return accountEnabled;
    }
}
