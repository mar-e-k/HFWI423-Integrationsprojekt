package de.fhdw.vendix.commons.spring.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.security.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.security.context.store.StoreContext;
import de.fhdw.vendix.commons.spring.security.user_details.DefaultUser;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    // TODO: Cache this to make it less expensive
    @Override
    public String generateToken() {
        try {
            Date now = new Date();
            Date exp = new Date(now.getTime() + expiration.toMillis());

            Long storeId = storeContext.getStore() == null ? null : storeContext.getStore().id();
            Long registerId = registerContext.getRegister() == null ? null : registerContext.getRegister().id();

            String subject;
            Set<Role> roles;

            if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof DefaultUser defaultUser) {
                subject = defaultUser.authContext().account().uuid().toString();
                roles = defaultUser.authContext().roles();
            } else {
                subject = appContext.getInstanceUUID().toString();
                roles = Set.of(Role.SYSTEM);
            }

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issuer(appContext.getApplicationName())
                    .audience(List.of("orchestrator", "pos", "store"))
                    .issueTime(now)
                    .expirationTime(exp)
                    .subject(subject)
                    .claim(JwtContextClaims.ROLES.claim(), roles)
                    .claim(JwtContextClaims.STORE.claim(), storeId)
                    .claim(JwtContextClaims.REGISTER.claim(), registerId)
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
    public JWTClaimsSet parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret);

            if (!signedJWT.verify(verifier)) {
                throw new BadCredentialsException("Invalid JWT signature");
            }

            return signedJWT.getJWTClaimsSet();

        } catch (ParseException e) {
            throw new BadCredentialsException("Invalid JWT format", e);
        } catch (JOSEException e) {
            throw new BadCredentialsException("JWT verification failed", e);
        }
    }
}