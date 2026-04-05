package de.fhdw.vendix.commons.spring.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;

public interface JwtService {
    String generateToken();
    JWTClaimsSet parseToken(String token);
}