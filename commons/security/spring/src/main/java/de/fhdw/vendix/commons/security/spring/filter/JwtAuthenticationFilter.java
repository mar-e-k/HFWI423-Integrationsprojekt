package de.fhdw.vendix.commons.security.spring.filter;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.security.core.JwtService;
import de.fhdw.vendix.security.api.jwt.JwtPayload;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public final class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AccountQueryPort accountQueryPort;

    public JwtAuthenticationFilter(JwtService jwtService, AccountQueryPort accountQueryPort) {
        this.jwtService = jwtService;
        this.accountQueryPort = accountQueryPort;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws IOException, ServletException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            try {
                Authentication authentication = resolveAuthentication(header.substring(7));
                SecurityContext context = SecurityContextHolder.createEmptyContext(); // Avoids edge-case auth leaking in multithreading environment
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    // TODO: this is here temporarily. Will be refactored to be a separate component that the filter will call with a JwtPayload or similar
    private Authentication resolveAuthentication(String token) throws AuthenticationException {
        JwtPayload payload = jwtService.parseToken(token);
        Set<AccountRoleEnum> accountRoleEnums = payload.auth().accountRoleEnums();

        if (accountRoleEnums.isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Invalid token. Token has no roles defined");
        }

        AccountDTO account;

        if (accountRoleEnums.size() == 1 && accountRoleEnums.contains(AccountRoleEnum.SYSTEM)) {
            account = new AccountDTO(
                    null,
                    payload.auth().subject(),
                    "system",
                    "system"
            );
        } else {
            account = accountQueryPort.findByUUID(payload.auth().subject())
                    .orElseThrow(() -> new BadCredentialsException("Invalid token. Account with specified UUID does not exist"));
        }

        return new UsernamePasswordAuthenticationToken(account.username(), account.password());
    }
}