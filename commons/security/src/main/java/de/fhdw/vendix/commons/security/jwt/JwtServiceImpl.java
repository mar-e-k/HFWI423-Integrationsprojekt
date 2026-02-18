package de.fhdw.vendix.commons.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.commons.security.jwt.claims.AuthClaims;
import de.fhdw.vendix.commons.security.jwt.claims.ContextClaims;
import de.fhdw.vendix.commons.security.jwt.claims.JwtClaimsEnum;
import de.fhdw.vendix.commons.security.jwt.claims.JwtPayload;
import org.springframework.expression.ParseException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
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
            if (expirationTime == null || expirationTime.before(new Date())) {
                throw new IllegalStateException("JWT is expired");
            }

            List<String> roleValues = claims.getStringListClaim(
                    JwtClaimsEnum.ACCOUNT_ROLES.claim()
            );

            Set<AccountRoleEnum> roles;
            if (roleValues == null || roleValues.isEmpty()) {
                roles = Set.of();
            } else {
                try {
                    roles = roleValues.stream()
                            .map(AccountRoleEnum::valueOf)
                            .collect(Collectors.toUnmodifiableSet());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalStateException("Invalid account role in JWT", ex);
                }
            }

            Long storeId = claims.getLongClaim(JwtClaimsEnum.STORE_ID.claim());
            Long registerId = claims.getLongClaim(JwtClaimsEnum.REGISTER_ID.claim());

            return new JwtPayload(
                    AuthClaims.user(
                            claims.getSubject(),
                            roles
                    ),
                    ContextClaims.context(
                            storeId,
                            registerId
                    )
            );

        } catch (ParseException e) {
            throw new IllegalStateException("Invalid JWT format", e);
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT verification failed", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JWT", e);
        }
    }
}