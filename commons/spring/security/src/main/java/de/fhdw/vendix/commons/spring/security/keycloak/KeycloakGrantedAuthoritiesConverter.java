package de.fhdw.vendix.commons.spring.security.keycloak;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;

public final class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter scopesConverter;
    private final KeycloakAuthoritiesExtractor authoritiesExtractor;

    public KeycloakGrantedAuthoritiesConverter(KeycloakAuthoritiesExtractor authoritiesExtractor) {
        this.scopesConverter = new JwtGrantedAuthoritiesConverter();
        this.authoritiesExtractor = authoritiesExtractor;
    }

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        Collection<GrantedAuthority> scopeAuthorities = scopesConverter.convert(jwt);
        authorities.addAll(scopeAuthorities);
        authorities.addAll(
                authoritiesExtractor.extractAuthorities(
                        jwt.getClaims()
                )
        );

        return Collections.unmodifiableSet(authorities);
    }
}