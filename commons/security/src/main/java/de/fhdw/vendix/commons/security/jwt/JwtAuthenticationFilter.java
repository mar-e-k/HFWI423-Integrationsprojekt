package de.fhdw.vendix.commons.security.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface JwtAuthenticationFilter {
    Authentication resolveAuthentication(String token) throws AuthenticationException;
}