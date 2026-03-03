package de.fhdw.vendix.commons.security.core;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ExpiredJWTException;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.security.api.jwt.claims.JwtClaimsEnum;
import de.fhdw.vendix.security.api.jwt.JwtPayload;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class JwtServiceImpl implements JwtService {

    private final byte[] secret;
    private final Duration expiration;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.secret = jwtProperties.privateKey().getBytes(StandardCharsets.UTF_8);
        this.expiration = jwtProperties.expiration();
    }

    @Override
    public String generateToken(JwtPayload payload) {
        try {
            Date now = new Date();
            Date exp = new Date(now.getTime() + expiration.toMillis());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(now)
                    .expirationTime(exp)
                    .subject(payload.auth().subject().toString())
                    .claim(JwtClaimsEnum.ROLES.claim(), payload.auth().accountRoleEnums())
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
            Set<AccountRoleEnum> accountRoleEnums = claims.getStringListClaim(JwtClaimsEnum.ROLES.claim()).stream()
                    .map(AccountRoleEnum::valueOf)
                    .collect(Collectors.toUnmodifiableSet());

            return JwtPayload.user(uuid, accountRoleEnums);

        } catch (ParseException e) {
            throw new IllegalStateException("Invalid JWT format", e);
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT verification failed", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JWT", e);
        }
    }
}