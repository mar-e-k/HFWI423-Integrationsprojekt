package de.fhdw.vendix.commons.security.core;

import de.fhdw.vendix.security.api.jwt.JwtPayload;

public interface JwtService {
    String generateToken(JwtPayload jwtPayload);
    JwtPayload parseToken(String token);
}