package de.fhdw.vendix.commons.spring.security.jwt;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.spring.security.authentication.AuthenticationService;
import de.fhdw.vendix.commons.spring.security.jwt.payload.JwtPayload;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthenticationService authenticationPort;

    private final Map<UUID, AccountDTO> cachedAccounts = new ConcurrentHashMap<>();

    public JwtAuthenticationFilter(JwtService jwtService, AuthenticationService authenticationPort) {
        this.jwtService = jwtService;
        this.authenticationPort = authenticationPort;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws IOException, ServletException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.split(" ")[1]; // Gets the token after 'Bearer ...'
                Authentication authentication = resolveAuthentication(token);
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
        Set<Role> roles = payload.auth().roles();

        if (roles.isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Invalid token. Token has no roles defined");
        }

        if (roles.contains(Role.SYSTEM)) {
            return new UsernamePasswordAuthenticationToken(
                    "system",
                    "system"
            );
        } else {
            AccountDTO account = cachedAccounts.computeIfAbsent(
                    payload.auth().subject(),
                    uuid -> authenticationPort.findByUUID(uuid)
                            .orElseThrow(() -> new BadCredentialsException("Invalid token. Account with specified UUID does not exist"))
            );

            return new UsernamePasswordAuthenticationToken(
                    account.username(),
                    account.password()
            );
        }
    }
}