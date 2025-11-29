package de.fhdw.fillialensystem.utility.security;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountRole;
import de.fhdw.fillialensystem.persistence.service.AccountService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parseToken(header.substring(7)); //JWT Tokens start

                Account account;

                if (claims.get("username", String.class).equalsIgnoreCase("system")
                        && claims.get("role", String.class).equalsIgnoreCase(AccountRoleEnum.SYSTEM.name())) {
                    account = new Account(
                            new AccountRole(null, AccountRoleEnum.SYSTEM),
                            null,
                            claims.getSubject(),
                            "system",
                            "system");
                } else {
                    String uuid = claims.getSubject();
                    account = accountService.findByUuid(uuid)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + uuid));
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                account, null,
                                account.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}