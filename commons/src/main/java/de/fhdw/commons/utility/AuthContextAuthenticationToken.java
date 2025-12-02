package de.fhdw.commons.utility;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class AuthContextAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final AuthContext authContext;

    public AuthContextAuthenticationToken(AuthContext authContext) {
        super(List.of(new SimpleGrantedAuthority("ROLE_".concat(authContext.getAccountRole().name()))));
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

    public AuthContext getAuthContext() {
        return authContext;
    }
}