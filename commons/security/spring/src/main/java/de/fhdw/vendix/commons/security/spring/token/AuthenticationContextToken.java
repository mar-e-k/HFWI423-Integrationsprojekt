package de.fhdw.vendix.commons.security.spring.token;

import de.fhdw.vendix.security.api.authentication.AuthContext;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public final class AuthenticationContextToken extends AbstractAuthenticationToken {

    private final AuthContext authContext;

    public AuthenticationContextToken(AuthContext authContext) {
        Set<SimpleGrantedAuthority> authorities = authContext.accountRoles()
                .stream()
                .map(Enum::name)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
        super(authorities);
        super.setAuthenticated(true);
        this.authContext = authContext;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return authContext;
    }

    @Override
    public @Nullable Object getCredentials() {
        return authContext;
    }
}