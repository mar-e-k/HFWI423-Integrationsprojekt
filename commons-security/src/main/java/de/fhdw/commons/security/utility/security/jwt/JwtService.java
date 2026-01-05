package de.fhdw.commons.security.utility.security.jwt;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.security.utility.config.JwtProperties;
import de.fhdw.commons.security.utility.security.jwt.claims.AuthClaims;
import de.fhdw.commons.security.utility.security.jwt.claims.ContextClaims;
import de.fhdw.commons.security.utility.security.jwt.claims.JwtClaimsEnum;
import de.fhdw.commons.security.utility.security.jwt.claims.JwtPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class JwtService {

    private final SecretKey secretKey;
    private final Duration expiration;

    public JwtService(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.getPrivateKey().getBytes(StandardCharsets.UTF_8));
        this.expiration = properties.getExpiration();
    }

    public String generateToken(JwtPayload payload) { // TODO: set Issuer and Assignee
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration.toMillis()))
                .signWith(secretKey)
                .subject(payload.auth().accountUuid())
                .claim(JwtClaimsEnum.ACCOUNT_ROLES.claim(),
                        payload.auth().accountRoles()
                                .stream()
                                .map(Enum::name)
                                .toList())
                .claim(JwtClaimsEnum.STORE_ID.claim(),
                        payload.ctx().storeId())
                .claim(JwtClaimsEnum.REGISTER_ID.claim(),
                        payload.ctx().registerId())
                .compact();
    }

    public JwtPayload parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<?> roleValues = claims.get(JwtClaimsEnum.ACCOUNT_ROLES.claim(), List.class);

        Set<AccountRoleEnum> roles;
        if (roleValues == null || roleValues.isEmpty()) {
            roles = Set.of();
        } else {
            try {
                roles = roleValues.stream()
                        .map(o -> {
                            if (!(o instanceof String s)) {
                                throw new JwtException("Invalid role claim type: " + o.getClass());
                            }
                            return AccountRoleEnum.valueOf(s);
                        })
                        .collect(Collectors.toUnmodifiableSet());
            } catch (IllegalArgumentException ex) {
                throw new JwtException("Invalid account role in JWT", ex);
            }
        }

        return new JwtPayload(
                AuthClaims.user(
                        claims.getSubject(),
                        roles),
                ContextClaims.context(
                        claims.get(JwtClaimsEnum.STORE_ID.claim(), Long.class),
                        claims.get(JwtClaimsEnum.REGISTER_ID.claim(), Long.class)
                )
        );
    }
}