package de.fhdw.vendix.commons.spring.security.jwt.payload;

import de.fhdw.vendix.commons.spring.security.jwt.payload.claims.AuthClaims;
import de.fhdw.vendix.commons.spring.security.jwt.payload.claims.ContextClaims;

public record JwtPayload(
        AuthClaims auth,
        ContextClaims ctx
) {}