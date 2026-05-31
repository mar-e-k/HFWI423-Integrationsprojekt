package de.fhdw.vendix.commons.spring.security.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakAuthoritiesExtractorTest {

    @Test
    void mapsKnownRealmRolesToAuthorities() {
        Map<String, Object> claims = Map.of("realm_access", Map.of("roles", List.of("admin", "cashier", "unknown")));

        Collection<GrantedAuthority> authorities = new KeycloakAuthoritiesExtractor().extractAuthorities(claims);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "ROLE_CASHIER")
                .doesNotContain("unknown");
    }

    @Test
    void returnsEmptyCollectionWhenRealmAccessIsMissing() {
        assertThat(new KeycloakAuthoritiesExtractor().extractAuthorities(Map.of())).isEmpty();
    }
}
