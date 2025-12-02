package de.fhdw.fillialensystem.utility.security;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.utility.AuthContext;
import de.fhdw.commons.utility.AuthContextAuthenticationToken;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AccountService accountService;

    public JwtAuthenticationFilter(JwtService jwtService, AccountService accountService) {
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtService.parseToken(token);
                AuthContext authContext;

                if (claims.get("role", String.class).equalsIgnoreCase("SYSTEM")) {
                    authContext = new AuthContext(
                            AccountRoleEnum.valueOf(claims.get("role", String.class)),
                            claims.getSubject(),
                            claims.get("username", String.class),
                            claims.get("password", String.class),
                            claims.get("storeId", Integer.class),
                            claims.get("registerId", Integer.class)
                    );
                } else {
                    Account account = accountService.findByUuid(claims.getSubject())
                            .orElseThrow(AccountNotFoundException::new);

                    authContext = new AuthContext(
                            account.getAccountRole().getRole(),
                            account.getUuid(),
                            account.getUsername(),
                            account.getPassword(),
                            claims.get("storeId", Integer.class),
                            claims.get("registerId", Integer.class)
                    );
                }

                SecurityContextHolder.getContext().setAuthentication(
                        new AuthContextAuthenticationToken(authContext)
                );

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}