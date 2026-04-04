package de.fhdw.vendix.commons.spring.security.user_details;

import de.fhdw.vendix.commons.spring.security.context.auth.AuthContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public record DefaultUser(
        AuthContext ctx,
        boolean isAccountNonExpired,
        boolean isAccountNonLocked,
        boolean isCredentialsNonExpired,
        boolean isEnabled
) implements UserDetails {
    public DefaultUser {
        if (ctx == null) {
            throw new IllegalArgumentException("DefaultUser parameter 'ctx' cannot be null");
        }
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return ctx.roles().stream()
                .map(Enum::name)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Nullable String getPassword() {
        return ctx.account().password();
    }

    @Override
    public @NonNull String getUsername() {
        return ctx.account().username();
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