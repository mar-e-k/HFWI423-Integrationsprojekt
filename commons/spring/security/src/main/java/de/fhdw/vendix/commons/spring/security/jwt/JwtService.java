package de.fhdw.vendix.commons.spring.security.jwt;

import de.fhdw.vendix.commons.spring.security.jwt.payload.JwtPayload;

public interface JwtService {
    String generateToken();
    JwtPayload parseToken(String token);
}