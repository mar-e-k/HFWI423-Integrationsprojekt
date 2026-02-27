package de.fhdw.vendix.commons.security.spring;

import de.fhdw.vendix.commons.api.domain.role.dto.RoleDTO;
import de.fhdw.vendix.security.api.auth.AuthContext;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.stream.Collectors;

public record SpringUserDetailsAdapter(
        AuthContext ctx
) implements UserDetails, Principal {
    public SpringUserDetailsAdapter {
        if (ctx == null) {
            throw new IllegalArgumentException("SpringUserDetailsAdapter parameter 'ctx' cannot be null");
        }
    }

    @Override
    public String getName() {
        return ctx.account().username();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return ctx.account().roles().stream()
                .map(RoleDTO::role)
                .map(Enum::name)
                .map("ROLE_"::concat)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Nullable String getPassword() {
        return ctx.account().password();
    }

    @Override
    public String getUsername() {
        return ctx.account().username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return ctx.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return ctx.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return ctx.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return ctx.isEnabled();
    }
}