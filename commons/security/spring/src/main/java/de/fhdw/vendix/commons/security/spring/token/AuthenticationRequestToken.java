package de.fhdw.vendix.commons.security.spring.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public final class AuthenticationRequestToken extends AbstractAuthenticationToken {

    private final String identifier;
    private final String password;

    public AuthenticationRequestToken(String identifier, String password) {
        super((Collection<? extends GrantedAuthority>) null);
        super.setAuthenticated(false);
        this.identifier = identifier;
        this.password = password;
    }

    @Override
    public Object getPrincipal() {
        return identifier;
    }

    @Override
    public Object getCredentials() {
        return password;
    }
}