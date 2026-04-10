package de.fhdw.vendix.commons.spring.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.AccountProxyService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtValidator jwtValidator;
    private final AccountProxyService accountProxyService;

    private final Map<UUID, AccountDTO> cachedAccounts = new ConcurrentHashMap<>();

    public JwtAuthenticationFilter(JwtService jwtService, JwtValidator jwtValidator, AccountProxyService accountProxyService) {
        this.jwtService = jwtService;
        this.jwtValidator = jwtValidator;
        this.accountProxyService = accountProxyService;
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

    private Authentication resolveAuthentication(String token) {
        JWTClaimsSet claims = jwtService.parseToken(token);

        jwtValidator.validate(claims);

        UUID subject = UUID.fromString(claims.getSubject());
        Set<Role> roles;

        try {
            roles = claims.getStringListClaim(JwtClaims.ROLES.claim()).stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
        } catch (ParseException e) {
            throw new BadCredentialsException("Invalid roles format", e);
        }

        Set<GrantedAuthority> authorities =
                roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toUnmodifiableSet());

        if (roles.contains(Role.SYSTEM)) {
            return new UsernamePasswordAuthenticationToken(
                    "system-" + subject,
                    null,
                    authorities
            );
        }

        AccountDTO account = cachedAccounts.computeIfAbsent(
                subject,
                uuid -> Optional.ofNullable(accountProxyService.getAccountByUuid(uuid).getBody())
                        .orElseThrow(() -> new BadCredentialsException("Account not found"))
        );

        return new UsernamePasswordAuthenticationToken(
                account.username() + "-" + subject,
                null,
                authorities
        );
    }
}