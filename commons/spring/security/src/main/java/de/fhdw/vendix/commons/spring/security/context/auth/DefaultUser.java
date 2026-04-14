package de.fhdw.vendix.commons.spring.security.context.auth;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public record DefaultUser(
        AuthContext authContext,
        boolean isAccountNonExpired,
        boolean isAccountNonLocked,
        boolean isCredentialsNonExpired,
        boolean isEnabled
) implements UserDetails {
    public DefaultUser {
        if (authContext == null) {
            throw new IllegalArgumentException("DefaultUser parameter 'authContext' cannot be null");
        }
    }

    public DefaultUser(AuthContext authContext) {
        this(
                authContext,
                true,
                true,
                true,
                true
        );
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authContext.roles().stream()
                .map(Enum::name)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Nullable String getPassword() {
        return authContext.account().password();
    }

    @Override
    public @NonNull String getUsername() {
        return authContext.account().username();
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