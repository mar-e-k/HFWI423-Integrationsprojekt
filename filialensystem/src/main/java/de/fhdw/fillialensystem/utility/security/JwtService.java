package de.fhdw.fillialensystem.utility.security;

import de.fhdw.commons.utility.AuthContext;
import de.fhdw.commons.utility.AuthContextAuthenticationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;

    public JwtService(@Value("${spring.security.private-key}") String privateKey) {
        secretKey = Keys.hmacShaKeyFor(privateKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AuthContext authContext) {
        return Jwts.builder()
                .subject(authContext.getUuid())
                .claim("username", authContext.getUsername())
                .claim("role", authContext.getAccountRole())
                .claim("storeId", authContext.getStoreId())
                .claim("registerId", authContext.getRegisterId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000)) // 1 hour
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Optional<AuthContext> getCurrentAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof AuthContext token) {
            return Optional.of(token);
        }
        return Optional.empty();
    }
}