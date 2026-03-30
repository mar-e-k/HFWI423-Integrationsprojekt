package de.fhdw.vendix.commons.security.core.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ExpiredJWTException;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.security.api.context.AppContext;
import de.fhdw.vendix.security.api.context.RegisterContext;
import de.fhdw.vendix.security.api.context.StoreContext;
import de.fhdw.vendix.security.api.jwt.JwtProperties;
import de.fhdw.vendix.security.api.jwt.JwtService;
import de.fhdw.vendix.security.api.jwt.payload.claims.AuthClaims;
import de.fhdw.vendix.security.api.jwt.payload.claims.ContextClaims;
import de.fhdw.vendix.security.api.jwt.payload.claims.JwtClaims;
import de.fhdw.vendix.security.api.jwt.payload.JwtPayload;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DefaultJwtService implements JwtService {

    private final AppContext appContext;
    private final StoreContext storeContext;
    private final RegisterContext registerContext;

    private final byte[] secret;
    private final Duration expiration;

    public DefaultJwtService(
            AppContext appContext,
            StoreContext storeContext,
            RegisterContext registerContext,
            JwtProperties jwtProperties
    ) {
        this.appContext = appContext;
        this.storeContext = storeContext;
        this.registerContext = registerContext;
        this.secret = jwtProperties.privateKey().getBytes(StandardCharsets.UTF_8);
        this.expiration = jwtProperties.expiration();
    }

    @Override
    public String generateToken() {
        try {
            Date now = new Date();
            Date exp = new Date(now.getTime() + expiration.toMillis());

            Long storeId = storeContext.getStore() == null ? null : storeContext.getStore().id();
            Long registerId = registerContext.getRegister() == null ? null : registerContext.getRegister().id();

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(now)
                    .expirationTime(exp)
                    .subject(appContext.getInstanceUUID().toString())
                    .claim(JwtClaims.ROLES.claim(), Set.of(AccountRole.values())) // TODO
                    .claim(JwtClaims.STORE.claim(), storeId)
                    .claim(JwtClaims.REGISTER.claim(), registerId)
                    .build();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            JWSSigner signer = new MACSigner(secret);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to generate JWT", e);
        }
    }

    @Override
    public JwtPayload parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret);

            if (!signedJWT.verify(verifier)) {
                throw new IllegalStateException("Invalid JWT signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // Expiration validation
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime == null || expirationTime.before(Date.from(Instant.now()))) {
                throw new ExpiredJWTException("JWT is expired");
            }

            UUID uuid = UUID.fromString(claims.getSubject());
            Set<AccountRole> accountRoles = claims.getStringListClaim(JwtClaims.ROLES.claim()).stream()
                    .map(AccountRole::valueOf)
                    .collect(Collectors.toUnmodifiableSet());

            AuthClaims authClaims = new AuthClaims(uuid, accountRoles);
            ContextClaims contextClaims = new ContextClaims();

            return new JwtPayload(authClaims, contextClaims);

        } catch (ParseException e) {
            throw new IllegalStateException("Invalid JWT format", e);
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT verification failed", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JWT", e);
        }
    }
}