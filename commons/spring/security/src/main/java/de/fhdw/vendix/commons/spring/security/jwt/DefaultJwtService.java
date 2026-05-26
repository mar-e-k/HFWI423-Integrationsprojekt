package de.fhdw.vendix.commons.spring.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.spring.security.context.auth.DefaultUser;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public final class DefaultJwtService implements JwtService {

    private final byte[] secret;
    private final Duration expiration;
    private final Object systemTokenMonitor = new Object();

    private volatile @Nullable String cachedSystemToken;
    private volatile long cachedSystemTokenRefreshAtMillis;

    public DefaultJwtService(JwtProperties jwtProperties) {
        this.secret = jwtProperties.privateKey().getBytes(StandardCharsets.UTF_8);
        this.expiration = jwtProperties.expiration();
    }

    @Override
    public String generateToken() {
        DefaultUser defaultUser = currentUser();
        if (defaultUser != null) {
            Date now = new Date();
            Date exp = new Date(now.getTime() + expiration.toMillis());
            return createToken(
                    defaultUser.authContext().account().uuid().toString(),
                    defaultUser.authContext().roles(),
                    now,
                    exp
            );
        }

        return generateSystemToken();
    }

    private @Nullable DefaultUser currentUser() {
        @Nullable Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DefaultUser defaultUser) {
            return defaultUser;
        }
        return null;
    }

    private String generateSystemToken() {
        long nowMillis = System.currentTimeMillis();
        @Nullable String token = cachedSystemToken;
        if (token != null && nowMillis < cachedSystemTokenRefreshAtMillis) {
            return token;
        }

        synchronized (systemTokenMonitor) {
            nowMillis = System.currentTimeMillis();
            token = cachedSystemToken;
            if (token != null && nowMillis < cachedSystemTokenRefreshAtMillis) {
                return token;
            }

            Date now = new Date(nowMillis);
            Date exp = new Date(nowMillis + expiration.toMillis());
            String newToken = createToken(UUID.randomUUID().toString(), Set.of(Role.SYSTEM), now, exp);
            cachedSystemToken = newToken;
            cachedSystemTokenRefreshAtMillis = nextSystemTokenRefreshAt(nowMillis);
            return newToken;
        }
    }

    private long nextSystemTokenRefreshAt(long issuedAtMillis) {
        long ttlMillis = expiration.toMillis();
        if (ttlMillis <= 1_000L) {
            return issuedAtMillis;
        }
        return issuedAtMillis + Math.max(1_000L, ttlMillis * 9 / 10);
    }

    private String createToken(String subject, Set<Role> roles, Date now, Date exp) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(now)
                    .expirationTime(exp)
                    .subject(subject)
                    .claim(JwtClaims.ROLES.claim(), roles)
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(new MACSigner(secret));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to generate JWT", e);
        }
    }

    @Override
    public JWTClaimsSet parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret);

            if (!signedJWT.verify(verifier)) {
                throw new BadCredentialsException("Invalid JWT signature");
            }

            @Nullable JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            if (claimsSet == null) {
                throw new BadCredentialsException("JWT claims missing");
            }
            return claimsSet;

        } catch (ParseException e) {
            throw new BadCredentialsException("Invalid JWT format", e);
        } catch (JOSEException e) {
            throw new BadCredentialsException("JWT verification failed", e);
        }
    }
}
