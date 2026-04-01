package de.fhdw.vendix.commons.security.jwt;

import jakarta.servlet.Filter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface JwtAuthenticationFilter extends Filter {
    Authentication resolveAuthentication(String token) throws AuthenticationException;
}