package de.fhdw.vendix.commons.security.spring;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.security.api.auth.AuthContext;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public final class AuthContextAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthContext ctx;

    public AuthContextAuthenticationToken(SpringUserDetailsAdapter adapter) {
        super(adapter.getAuthorities());
        super.setAuthenticated(true);
        this.ctx = adapter.ctx();
    }

    public AuthContextAuthenticationToken(AuthContext ctx) {
        Set<SimpleGrantedAuthority> authorities = ctx.account().roles().stream()
                .map(AccountRoleDTO::role)
                .map(Enum::name)
                .map("ROLE_"::concat)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
        super(authorities);
        super.setAuthenticated(true);
        this.ctx = ctx;
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return ctx;
    }
}