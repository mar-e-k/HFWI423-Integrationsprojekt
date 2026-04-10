package de.fhdw.vendix.commons.spring.security.user_details;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.security.context.auth.AuthContext;
import de.fhdw.vendix.commons.spring.security.context.auth.DefaultAuthContext;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.AccountProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.DistributedLockProxyService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DefaultUserDetailsService implements UserDetailsService {

    private final AccountProxyService accountProxyService;
    private final DistributedLockProxyService distributedLockProxyService;

    public DefaultUserDetailsService(AccountProxyService accountProxyService, DistributedLockProxyService distributedLockProxyService) {
        this.accountProxyService = accountProxyService;
        this.distributedLockProxyService = distributedLockProxyService;
    }


    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        AccountDTO account = resolveAccount(username)
                .orElseThrow(() -> new UsernameNotFoundException("Identifier doesnt reference an account"));

        Objects.requireNonNull(account.id());

        Set<Role> roles = Objects.requireNonNull(accountProxyService.getAccountRolesById(account.id()).getBody()).stream()
                .map(AccountRoleDTO::role)
                .collect(Collectors.toUnmodifiableSet());

        boolean isAccountNonLocked = distributedLockProxyService.getDistributedLockByTarget(
                TargetType.ACCOUNT,
                account.id()
        ).getStatusCode().is4xxClientError();

        AuthContext authContext = new DefaultAuthContext(
                account,
                roles
        );

        return new DefaultUser(
                authContext,
                true,
                isAccountNonLocked,
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
            return Optional.ofNullable(accountProxyService.getAccountByUuid(uuid).getBody());
        } catch (IllegalArgumentException ignored) {}
//        if (identifier.contains("@")) {
//            return accountProxyService.getAccountByEmail(identifier);
//        }
//
//        if (identifier.matches("\\+?[0-9]+")) {
//            return accountProxyService.getAccountByPhone(identifier);
//        }

        return Optional.ofNullable(accountProxyService.getAccountByUsername(identifier).getBody());
    }
}