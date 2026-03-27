package de.fhdw.vendix.commons.security.spring.authentication;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.security.api.authentication.AuthenticationQueryPort;
import de.fhdw.vendix.security.api.authorization.AuthorizationQueryPort;
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

    private final AuthenticationQueryPort authenticationQueryPort;
    private final AuthorizationQueryPort authorizationQueryPort;

    public DefaultUserDetailsService(AuthenticationQueryPort authenticationQueryPort, AuthorizationQueryPort authorizationQueryPort) {
        this.authenticationQueryPort = authenticationQueryPort;
        this.authorizationQueryPort = authorizationQueryPort;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws AuthenticationException {
        AccountDTO account = resolveAccount(username)
                .orElseThrow(() -> new BadCredentialsException("Identifier doesnt reference an account"));

        Objects.requireNonNull(account.id(), "this really shouldn't happen");

        Set<AccountRoleEnum> roles = authorizationQueryPort.findRolesByAccountId(account.id());

        boolean isAccountLocked = authorizationQueryPort.isAccountLocked(account.id());

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
            return authenticationQueryPort.findByUUID(uuid);
        } catch (IllegalArgumentException ignored) {}

        if (identifier.contains("@")) {
            return authenticationQueryPort.findByEmail(identifier);
        }

        if (identifier.matches("\\+?[0-9]+")) {
            return authenticationQueryPort.findByPhone(identifier);
        }

        return authenticationQueryPort.findByUsername(identifier);
    }
}