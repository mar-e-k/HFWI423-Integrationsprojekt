package de.fhdw.vendix.commons.spring.security.keycloak;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

public final class KeycloakJwtAuthenticationConverter extends JwtAuthenticationConverter {

    public KeycloakJwtAuthenticationConverter(KeycloakGrantedAuthoritiesConverter authoritiesConverter) {
        setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    }
}