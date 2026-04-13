package de.fhdw.vendix.commons.spring.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultJwtValidator implements JwtValidator {

    public DefaultJwtValidator() {}

    public void validate(JWTClaimsSet payload) throws AuthenticationException {
        if (payload == null) {
            throw new IllegalArgumentException("Parameter 'payload' cannot be null");
        }
        validateExpiration(payload);
//        validateAudience(payload);
        validateIssuer(payload);
        validateRoles(payload);
    }

    private void validateExpiration(JWTClaimsSet claims) {
        Date expirationTime = claims.getExpirationTime();

        if (expirationTime == null || expirationTime.before(Date.from(Instant.now()))) {
            throw new BadCredentialsException("JWT expired");
        }
    }

//    private void validateAudience(JWTClaimsSet claims) {
//        String expectedAudience = appContext.getApplicationName();
//        List<String> audience = claims.getAudience();
//
//        if (audience == null || !audience.contains(expectedAudience)) {
//            throw new BadCredentialsException("Invalid audience");
//        }
//    }

    private void validateIssuer(JWTClaimsSet claims) {
        String issuer = claims.getIssuer();

        if (issuer == null || issuer.isBlank()) {
            throw new BadCredentialsException("Missing issuer");
        }
    }

    private void validateRoles(JWTClaimsSet claims) {
        List<String> roleStrings;

        try {
            roleStrings = claims.getStringListClaim(JwtClaims.ROLES.claim());
        } catch (ParseException e) {
            throw new BadCredentialsException("Invalid roles format", e);
        }

        if (roleStrings == null || roleStrings.isEmpty()) {
            throw new BadCredentialsException("No roles present");
        }

        Set<Role> roles = roleStrings.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}
