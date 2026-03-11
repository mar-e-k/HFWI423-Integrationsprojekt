package de.fhdw.vendix.commons.security.spring;

import de.fhdw.vendix.security.api.auth.AuthContext;
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
        Set<SimpleGrantedAuthority> authorities = ctx.accountRoles().stream()
                .map(Enum::name)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
        super(authorities);
        super.setAuthenticated(true);
        this.ctx = ctx;
    }

    @Override
    public Object getCredentials() {
        return ctx;
    }

    @Override
    public Object getPrincipal() {
        return ctx;
    }
}