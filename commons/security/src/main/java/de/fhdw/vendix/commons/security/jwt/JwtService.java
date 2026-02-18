package de.fhdw.vendix.commons.security.jwt;

import de.fhdw.vendix.commons.security.jwt.claims.JwtPayload;

public interface JwtService {
    String generateToken(JwtPayload jwtPayload);
    JwtPayload parseToken(String token);
}