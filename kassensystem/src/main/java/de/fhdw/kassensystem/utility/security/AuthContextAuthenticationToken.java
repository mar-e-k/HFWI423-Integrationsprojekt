package de.fhdw.kassensystem.utility.security;

import de.fhdw.commons.utility.AuthContext;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class AuthContextAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthContext authContext;

    public AuthContextAuthenticationToken(AuthContext authContext) {
        super(List.of(new SimpleGrantedAuthority("ROLE_".concat(authContext.getAccountRole().name()))));
        this.authContext = authContext;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return authContext;
    }

    public AuthContext getAuthContext() {
        return authContext;
    }
}
