package de.fhdw.vendix.commons.spring.security.keycloak;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.HashSet;
import java.util.Set;

public final class KeycloakOidcUserService extends OidcUserService {

    private final KeycloakAuthoritiesExtractor authoritiesExtractor;

    public KeycloakOidcUserService(KeycloakAuthoritiesExtractor authoritiesExtractor) {
        this.authoritiesExtractor = authoritiesExtractor;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        authorities.addAll(
                authoritiesExtractor.extractAuthorities(
                        oidcUser.getClaims()
                )
        );

        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }
}