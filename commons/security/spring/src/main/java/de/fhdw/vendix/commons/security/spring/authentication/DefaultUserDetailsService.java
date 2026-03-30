package de.fhdw.vendix.commons.security.spring.authentication;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.commons.security.core.context.DefaultAuthContext;
import de.fhdw.vendix.commons.security.spring.context.DefaultUser;
import de.fhdw.vendix.security.api.authentication.AuthenticationService;
import de.fhdw.vendix.security.api.authorization.AuthorizationService;
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

    private final AuthenticationService authenticationPort;
    private final AuthorizationService authorizationPort;

    public DefaultUserDetailsService(AuthenticationService authenticationPort, AuthorizationService authorizationPort) {
        this.authenticationPort = authenticationPort;
        this.authorizationPort = authorizationPort;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws AuthenticationException {
        AccountDTO account = resolveAccount(username)
                .orElseThrow(() -> new BadCredentialsException("Identifier doesnt reference an account"));

        Objects.requireNonNull(account.id(), "this really shouldn't happen");

        Set<AccountRole> roles = authorizationPort.findRolesByAccountId(account.id());

        boolean isAccountLocked = authorizationPort.isAccountLocked(account.id());

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
            return authenticationPort.findByUUID(uuid);
        } catch (IllegalArgumentException ignored) {}

        if (identifier.contains("@")) {
            return authenticationPort.findByEmail(identifier);
        }

        if (identifier.matches("\\+?[0-9]+")) {
            return authenticationPort.findByPhone(identifier);
        }

        return authenticationPort.findByUsername(identifier);
    }
}