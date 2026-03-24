package de.fhdw.vendix.security.api.jwt.payload;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.security.api.jwt.payload.claims.AuthClaims;
import de.fhdw.vendix.security.api.jwt.payload.claims.ContextClaims;

import java.util.Set;
import java.util.UUID;

public record JwtPayload(
        AuthClaims auth,
        ContextClaims ctx
) {
    public static JwtPayload system() {
        return new JwtPayload(
                new AuthClaims(
                        UUID.randomUUID(), // TODO: Change to an actual repeatable instance, like a global system-uuid from a spring bean
                        Set.of(AccountRoleEnum.SYSTEM)
                ),
                new ContextClaims()
        );
    }

    public static JwtPayload user(UUID uuid, Set<AccountRoleEnum> accountRoleEnums) {
        return new JwtPayload(
                new AuthClaims(
                        uuid,
                        accountRoleEnums
                ),
                new ContextClaims()
        );
    }
}