package de.fhdw.vendix.commons.security.spring.authentication;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
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

    private final AccountQueryPort accountQueryPort;
    private final LockQueryPort lockQueryPort;

    public DefaultUserDetailsService(AccountQueryPort accountQueryPort, LockQueryPort lockQueryPort) {
        this.accountQueryPort = accountQueryPort;
        this.lockQueryPort = lockQueryPort;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws AuthenticationException {
        AccountDTO account = resolveAccount(username)
                .orElseThrow(() -> new BadCredentialsException("Identifier doesnt reference an account"));

        Objects.requireNonNull(account.id(), "this really shouldn't happen");

        Set<AccountRoleEnum> roles = accountQueryPort.findAllRolesByAccount_Id(account.id());

        boolean isAccountLocked = lockQueryPort.existsByTargetTypeAndTargetID(TargetTypeEnum.ACCOUNT, account.id());

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

        // UUID
        try {
            UUID uuid = UUID.fromString(identifier);
            return accountQueryPort.findByUUID(uuid);
        } catch (IllegalArgumentException ignored) {
        }

        // Email
//        if (identifier.contains("@")) {
//            return accountQueryPort.findByEmail(identifier);
//        }

        // Phone
//        if (identifier.matches("\\+?[0-9]+")) {
//            return accountQueryPort.findByPhone(identifier);
//        }

        return accountQueryPort.findByUsername(identifier);
    }
}