package de.fhdw.vendix.store.utility.security;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.commons.security.jwt.AbstractJwtAuthenticationFilter;
import de.fhdw.vendix.commons.security.jwt.JwtService;
import de.fhdw.vendix.commons.security.jwt.claims.JwtPayload;
import de.fhdw.vendix.store.persistence.entity.Account;
import de.fhdw.vendix.store.persistence.service.AccountService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

public class JwtAuthenticationFilter extends AbstractJwtAuthenticationFilter {

    private final JwtService jwtService;
    private final AccountService accountService;

    public JwtAuthenticationFilter(JwtService jwtService, AccountService accountService) {
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    @Override
    public Authentication resolveAuthentication(String token) throws AuthenticationException {
        JwtPayload payload;

        try {
            payload = jwtService.parseToken(token);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JWT", e);
        }

        AuthContext authContext;

        if (payload.auth().roles().contains(AccountRoleEnum.SYSTEM)) {
            authContext = new AuthContext.Builder(null, payload.auth().subject(), null, null, payload.auth().roles())
                    .storeId(payload.ctx().storeId())
                    .registerId(payload.ctx().registerId())
                    .build();
        } else {
            Account account = accountService
                    .findByUuid(payload.auth().subject())
                    .orElseThrow(() -> new UsernameNotFoundException("Account not found for UUID " + payload.auth().subject()));

            authContext = new AuthContext.Builder(account.getId(), account.getUuid(), account.getUsername(), account.getPassword(), Set.of(account.getAccountRole().getRole()))
                    .storeId(payload.ctx().storeId())
                    .registerId(payload.ctx().registerId())
                    .build();
        }

        return new AuthContextAuthenticationToken(authContext);
    }
}