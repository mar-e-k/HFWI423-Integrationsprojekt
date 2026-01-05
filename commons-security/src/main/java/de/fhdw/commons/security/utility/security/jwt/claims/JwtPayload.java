package de.fhdw.commons.security.utility.security.jwt.claims;

import de.fhdw.commons.security.utility.security.auth.AuthContext;
import de.fhdw.commons.security.utility.security.auth.AuthContextHolder;

public record JwtPayload(
        AuthClaims auth,
        ContextClaims ctx
) {
    public static JwtPayload system() {
        return new JwtPayload(
                AuthClaims.system(),
                ContextClaims.empty()
        );
    }

    public static JwtPayload user() {
        AuthContext authContext = AuthContextHolder.current().orElseThrow(IllegalStateException::new);
        return new JwtPayload(
                AuthClaims.user(authContext.getUuid(), authContext.getRoles()),
                ContextClaims.context(authContext.getStoreId(), authContext.getRegisterId())
        );
    }

    public static JwtPayload user(AuthClaims authClaims, ContextClaims contextClaims) {
        return new JwtPayload(
                authClaims,
                contextClaims
        );
    }
}