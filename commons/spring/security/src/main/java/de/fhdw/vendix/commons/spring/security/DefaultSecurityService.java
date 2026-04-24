package de.fhdw.vendix.commons.spring.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

public final class DefaultSecurityService implements SecurityService {

    @Override
    public Optional<UUID> getAuthenticatedUserUuid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        // Case A: Browser-based Login (Vaadin UI)
        if (auth.getPrincipal() instanceof OidcUser oidcUser) {
            String subject = oidcUser.getSubject();
            return Optional.of(UUID.fromString(subject));
        }

        // Case B: API-based Token (k6 / REST)
        if (auth instanceof JwtAuthenticationToken jwtToken) {
            String subject = jwtToken.getToken().getSubject();
            return Optional.of(UUID.fromString(subject));
        }

        return Optional.empty();
    }
}