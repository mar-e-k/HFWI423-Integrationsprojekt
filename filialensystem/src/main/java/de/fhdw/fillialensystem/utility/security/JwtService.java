package de.fhdw.fillialensystem.utility.security;

import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.AccountRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;

    public JwtService(@Value("${spring.security.private-key}") String privateKey) {
        secretKey = Keys.hmacShaKeyFor(privateKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken() {
        Account currentAccount = getCurrentAccount();
        return Jwts.builder()
                .subject(currentAccount.getUuid())
                .claim("role", currentAccount.getAccountRole().getRole().name())
                .claim("username", currentAccount.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 500_000))
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

    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().toString().equalsIgnoreCase("anonymousUser")) {
            log.atWarn().log("Getting fallback system account for Token generation");
            return new Account(
                    new AccountRole(null, AccountRoleEnum.SYSTEM),
                    null,
                    UUID.randomUUID().toString(),
                    "system",
                    "system"
            );
        }
        if (auth.getPrincipal() instanceof Account account) {
            return account;
        } else {
            throw new AuthenticationServiceException("Cannot get current account from security context");
        }
    }
}