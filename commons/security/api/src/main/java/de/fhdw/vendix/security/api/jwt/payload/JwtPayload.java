package de.fhdw.vendix.security.api.jwt.payload;

import de.fhdw.vendix.security.api.jwt.payload.claims.AuthClaims;
import de.fhdw.vendix.security.api.jwt.payload.claims.ContextClaims;

public record JwtPayload(
        AuthClaims auth,
        ContextClaims ctx
) {}