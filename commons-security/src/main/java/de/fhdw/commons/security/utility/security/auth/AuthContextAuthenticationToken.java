package de.fhdw.commons.security.utility.security.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.io.Serial;
import java.io.Serializable;

public class AuthContextAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final AuthContext authContext;

    public AuthContextAuthenticationToken(AuthContext authContext) {
        super(authContext.getAuthorities());
        this.authContext = authContext;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return authContext;
    }
}