package de.fhdw.vendix.security.api;

import de.fhdw.vendix.security.api.jwt.JwtPayload;

public interface JwtService {
    String generateToken(JwtPayload jwtPayload);
    JwtPayload parseToken(String token);
}