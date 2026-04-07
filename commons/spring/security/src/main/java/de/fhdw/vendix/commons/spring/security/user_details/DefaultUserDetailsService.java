package de.fhdw.vendix.commons.spring.security.user_details;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.security.context.auth.AuthContext;
import de.fhdw.vendix.commons.spring.security.context.auth.DefaultAuthContext;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.AccountProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.LockProxyService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DefaultUserDetailsService implements UserDetailsService {

    private final AccountProxyService accountProxyService;
    private final LockProxyService lockProxyService;

    public DefaultUserDetailsService(AccountProxyService accountProxyService, LockProxyService lockProxyService) {
        this.accountProxyService = accountProxyService;
        this.lockProxyService = lockProxyService;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws AuthenticationException {
        AccountDTO account = resolveAccount(username)
                .orElseThrow(() -> new BadCredentialsException("Identifier doesnt reference an account"));

        Objects.requireNonNull(account.id());

        Set<Role> roles = Objects.requireNonNull(accountProxyService.getAccountRoles(account.uuid()).getBody()).stream()
                .map(AccountRoleDTO::role)
                .collect(Collectors.toUnmodifiableSet());

        boolean isAccountLocked = !Objects.requireNonNull(lockProxyService.getLocks(account.id(), TargetType.ACCOUNT, null).getBody()).isEmpty();

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
            return Optional.ofNullable(accountProxyService.getAccountByUUID(uuid).getBody());
        } catch (IllegalArgumentException ignored) {}
//        if (identifier.contains("@")) {
//            return authenticationPort.findByEmail(identifier);
//        }
//
//        if (identifier.matches("\\+?[0-9]+")) {
//            return authenticationPort.findByPhone(identifier);
//        }

        return Optional.ofNullable(accountProxyService.getAccountByUsername(identifier).getBody());
    }
}