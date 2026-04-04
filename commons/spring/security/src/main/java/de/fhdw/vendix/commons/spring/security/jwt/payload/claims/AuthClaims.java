package de.fhdw.vendix.commons.spring.security.jwt.payload.claims;

import de.fhdw.vendix.commons.api.domain.account_role.Role;

import java.util.Set;
import java.util.UUID;

public record AuthClaims(
        UUID subject,
        Set<Role> roles
) {
    public AuthClaims {
        if (subject == null) {
            throw new IllegalArgumentException("AuthClaims parameter 'subject' cannot be null");
        }
        if (roles == null) {
            throw new IllegalArgumentException("AuthClaims parameter 'subject' cannot be null");
        }
    }
}