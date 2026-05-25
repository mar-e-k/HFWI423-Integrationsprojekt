package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakAuthoritiesExtractor;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakGrantedAuthoritiesConverter;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakJwtAuthenticationConverter;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakOidcUserService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = SecurityWebAutoConfiguration.class)
public class KeycloakWebAutoConfiguration {

    @Bean
    public KeycloakAuthoritiesExtractor keycloakAuthoritiesExtractor() {
        return new KeycloakAuthoritiesExtractor();
    }

    @Bean
    public KeycloakGrantedAuthoritiesConverter keycloakGrantedAuthoritiesConverter(KeycloakAuthoritiesExtractor extractor) {
        return new KeycloakGrantedAuthoritiesConverter(extractor);
    }

    @Bean
    public KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter(KeycloakGrantedAuthoritiesConverter converter) {
        return new KeycloakJwtAuthenticationConverter(converter);
    }

    @Bean
    public KeycloakOidcUserService keycloakOidcUserService(KeycloakAuthoritiesExtractor extractor) {
        return new KeycloakOidcUserService(extractor);
    }
}