package de.fhdw.vendix.commons.security.spring.authentication;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.security.core.DefaultAuthContext;
import de.fhdw.vendix.commons.security.spring.token.AuthenticationContextToken;
import de.fhdw.vendix.commons.security.spring.token.AuthenticationRequestToken;
import de.fhdw.vendix.security.api.authentication.AuthContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DefaultAuthenticationProvider implements AuthenticationProvider {

    private final AccountQueryPort accountQueryPort;
    private final LockQueryPort lockQueryPort;

    private final PasswordEncoder passwordEncoder;

    public DefaultAuthenticationProvider(AccountQueryPort accountQueryPort, LockQueryPort lockQueryPort, PasswordEncoder passwordEncoder) {
        this.accountQueryPort = accountQueryPort;
        this.lockQueryPort = lockQueryPort;
        this.passwordEncoder = passwordEncoder;
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

    @Override
    public @Nullable Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        if (authentication.getPrincipal() == null) {
            throw new InsufficientAuthenticationException("Identifier wasn't provided");
        }
        if (authentication.getCredentials() == null) {
            throw new InsufficientAuthenticationException("Password wasn't provided");
        }

        String identifier = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();

        AccountDTO account = resolveAccount(identifier)
                .orElseThrow(() -> new BadCredentialsException("Identifier doesnt reference an account"));

        if (!passwordEncoder.matches(password, account.password())) {
            throw new BadCredentialsException("Passwords do not match");
        }

        Objects.requireNonNull(account.id());

        Set<AccountRoleEnum> roles = accountQueryPort.findAllRolesByAccount_Id(account.id());

        boolean locked = lockQueryPort.existsByTargetTypeAndTargetID(TargetTypeEnum.ACCOUNT, account.id());

        AuthContext authContext = new DefaultAuthContext(
                account,
                roles,
                true,
                locked,
                true,
                true
        );

        return new AuthenticationContextToken(authContext);
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return AuthenticationRequestToken.class.isAssignableFrom(authentication);
    }
}