package de.fhdw.vendix.commons.spring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class DefaultGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        authorities.forEach(authority -> {
            if (authority instanceof OidcUserAuthority oidcAuth) {
                var realmAccess = oidcAuth.getAttributes().get("realm_access");
            }
        });
        return mappedAuthorities;
    }
}