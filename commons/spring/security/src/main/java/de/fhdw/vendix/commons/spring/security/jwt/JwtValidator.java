package de.fhdw.vendix.commons.spring.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.security.core.AuthenticationException;

public interface JwtValidator {
    void validate(JWTClaimsSet payload) throws AuthenticationException;
}