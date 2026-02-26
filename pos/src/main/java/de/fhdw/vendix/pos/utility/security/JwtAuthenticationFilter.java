package de.fhdw.vendix.pos.utility.security;

import de.fhdw.vendix.commons.core.api.dto.AccountDTO;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.commons.security.jwt.AbstractJwtAuthenticationFilter;
import de.fhdw.vendix.commons.security.jwt.JwtService;
import de.fhdw.vendix.commons.security.jwt.claims.JwtPayload;
import de.fhdw.vendix.pos.persistance.service.proxy.AccountProxyService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JwtAuthenticationFilter extends AbstractJwtAuthenticationFilter {

    private final JwtService jwtService;
    private final AccountProxyService accountProxyService;

    public JwtAuthenticationFilter(JwtService jwtService, AccountProxyService accountProxyService) {
        this.jwtService = jwtService;
        this.accountProxyService = accountProxyService;
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
            AccountDTO account = accountProxyService
                    .findByUuid(payload.auth().subject())
                    .orElseThrow(() -> new UsernameNotFoundException("Account not found for UUID " + payload.auth().subject()));

            authContext = new AuthContext.Builder(account)
                    .storeId(payload.ctx().storeId())
                    .registerId(payload.ctx().registerId())
                    .build();
        }

        return new AuthContextAuthenticationToken(authContext);
    }
}