package de.fhdw.vendix.commons.spring.security.keycloak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class KeycloakAuthoritiesExtractor {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthoritiesExtractor.class);

    public Collection<GrantedAuthority> extractAuthorities(Map<String, Object> claims) {
        Set<String> rawRoles = new HashSet<>();
        Set<GrantedAuthority> authorities = new HashSet<>();

        extractRealmRoles(claims, rawRoles);

        for (String role : rawRoles) {
            KeycloakRole.from(role.toUpperCase().replace('-', '_')).ifPresentOrElse(
                    authorities::add,
                    () -> log.atWarn().log("Ignoring unknown Keycloak role: {}", role)
            );
        }

        log.atDebug().log("Mapped authorities: {}", authorities);

        return Set.copyOf(authorities);
    }

    private void extractRealmRoles(Map<String, Object> claims, Set<String> roles) {
        if (!(claims.get("realm_access") instanceof Map<?, ?> realmAccess)) {
            return;
        }
        if (!(realmAccess.get("roles") instanceof Collection<?> realmRoles)) {
            return;
        }
        realmRoles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .forEach(roles::add);
    }
}