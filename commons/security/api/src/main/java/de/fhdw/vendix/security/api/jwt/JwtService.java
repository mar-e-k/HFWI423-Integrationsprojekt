package de.fhdw.vendix.security.api.jwt;

import de.fhdw.vendix.security.api.jwt.payload.JwtPayload;

public interface JwtService {
    String generateToken(JwtPayload jwtPayload);
    JwtPayload parseToken(String token);
}