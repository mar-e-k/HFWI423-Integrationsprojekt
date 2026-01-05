package de.fhdw.kassensystem.utility.security;

import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.security.utility.security.auth.AuthContext;
import de.fhdw.commons.security.utility.security.auth.AuthContextAuthenticationToken;
import de.fhdw.commons.security.utility.security.jwt.AbstractJwtAuthenticationFilter;
import de.fhdw.commons.security.utility.security.jwt.JwtService;
import de.fhdw.commons.security.utility.security.jwt.claims.JwtPayload;
import de.fhdw.kassensystem.persistance.service.proxy.AccountProxyService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationFilter extends AbstractJwtAuthenticationFilter {

    private final JwtService jwtService;
    private final AccountProxyService accountProxyService;

    public JwtAuthenticationFilter(JwtService jwtService, AccountProxyService accountProxyService) {
        this.jwtService = jwtService;
        this.accountProxyService = accountProxyService;
    }

    @Override
    protected Authentication resolveAuthentication(String token) throws AuthenticationException {
        JwtPayload payload;

        try {
            payload = jwtService.parseToken(token);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JWT", e);
        }

        AuthContext authContext;

        if (payload.auth().accountRoles().contains(AccountRoleEnum.SYSTEM)) {
            authContext = new AuthContext.Builder(null, payload.auth().accountUuid(), null, null, payload.auth().accountRoles())
                    .storeId(payload.ctx().storeId())
                    .registerId(payload.ctx().registerId())
                    .build();
        } else {
            AccountDTO account = accountProxyService
                    .findByUuid(payload.auth().accountUuid())
                    .orElseThrow(() -> new UsernameNotFoundException("Account not found for UUID " + payload.auth().accountUuid()));

            authContext = new AuthContext.Builder(account)
                    .storeId(payload.ctx().storeId())
                    .registerId(payload.ctx().registerId())
                    .build();
        }

        return new AuthContextAuthenticationToken(authContext);
    }
}