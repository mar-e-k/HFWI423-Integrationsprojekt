package de.fhdw.vendix.commons.security.spring.authentication;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.specification.authentication.AuthenticationQueryApi;
import de.fhdw.vendix.commons.api.specification.authorization.AuthorizationQueryApi;
import de.fhdw.vendix.commons.security.core.DefaultAuthContext;
import de.fhdw.vendix.commons.security.spring.context.DefaultUser;
import de.fhdw.vendix.security.api.context.AuthContext;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DefaultUserDetailsService implements UserDetailsService {

    private final AuthenticationQueryApi authenticationQueryApi;
    private final AuthorizationQueryApi authorizationQueryApi;

    public DefaultUserDetailsService(AuthenticationQueryApi authenticationQueryApi, AuthorizationQueryApi authorizationQueryApi) {
        this.authenticationQueryApi = authenticationQueryApi;
        this.authorizationQueryApi = authorizationQueryApi;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws AuthenticationException {
        AccountDTO account = resolveAccount(username)
                .orElseThrow(() -> new BadCredentialsException("Identifier doesnt reference an account"));

        Objects.requireNonNull(account.id(), "this really shouldn't happen");

        Set<AccountRoleEnum> roles = authorizationQueryApi.findRolesByAccountId(account.id());

        boolean isAccountLocked = authorizationQueryApi.isAccountLocked(account.id());

        AuthContext authContext = new DefaultAuthContext(
                account,
                roles
        );

        return new DefaultUser(
                authContext,
                true,
                !isAccountLocked,
                true,
                true
        );
    }

    private Optional<AccountDTO> resolveAccount(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return Optional.empty();
        }

        try {
            UUID uuid = UUID.fromString(identifier);
            return authenticationQueryApi.findByUUID(uuid);
        } catch (IllegalArgumentException ignored) {}

        if (identifier.contains("@")) {
            return authenticationQueryApi.findByEmail(identifier);
        }

        if (identifier.matches("\\+?[0-9]+")) {
            return authenticationQueryApi.findByPhone(identifier);
        }

        return authenticationQueryApi.findByUsername(identifier);
    }
}