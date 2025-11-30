package de.fhdw.fillialensystem.utility.security;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.utility.AuthContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtService.parseToken(token);

                AuthContext authContext = new AuthContext(
                        AccountRoleEnum.valueOf(claims.get("role", String.class)),
                        claims.getSubject(),
                        claims.get("username", String.class),
                        claims.get("storeId", Long.class),
                        claims.get("registerId", Long.class)
                );

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