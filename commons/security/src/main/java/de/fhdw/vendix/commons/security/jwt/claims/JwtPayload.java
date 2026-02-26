package de.fhdw.vendix.commons.security.jwt.claims;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.role.dto.Role;
import de.fhdw.vendix.commons.api.domain.role.dto.RoleDTO;
import de.fhdw.vendix.commons.security.auth.AuthContext;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record JwtPayload(
        AuthClaims auth,
        ContextClaims ctx
) {
    public static JwtPayload system() {
        return new JwtPayload(
                new AuthClaims(
                        UUID.randomUUID(), // TODO: Change to an actual repeatable instance, like a global system-uuid from a spring bean
                        Set.of(Role.SYSTEM)
                ),
                new ContextClaims()
        );
    }

    public static JwtPayload user(UUID uuid, Set<Role> roles) {
        return new JwtPayload(
                new AuthClaims(
                        uuid,
                        roles
                ),
                new ContextClaims()
        );
    }

    public static JwtPayload user(AccountDTO account) {
        return user(
                account.uuid(),
                account.roles().stream()
                        .map(RoleDTO::role)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

    public static JwtPayload user(AuthContext ctx) {
        return user(ctx.account());
    }
}