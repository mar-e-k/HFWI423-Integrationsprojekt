package de.fhdw.vendix.commons.spring.security.keycloak;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class KeycloakJwtAuthenticationConverter extends JwtAuthenticationConverter {

    public KeycloakJwtAuthenticationConverter() {
        setJwtGrantedAuthoritiesConverter(this::extractGrantedAuthoritiesFromKeycloakAccessToken);
    }

    private List<GrantedAuthority> extractGrantedAuthoritiesFromKeycloakAccessToken(Jwt jwt) {
        Map<String, Object> realmAccess = Optional.ofNullable(jwt.getClaimAsMap("realm_access")).orElse(Map.of());

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?>)) {
            return List.of();
        }

        return ((List<?>) rolesObj).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
    }
}