package de.fhdw.vendix.commons.security.spring;

import de.fhdw.vendix.security.api.auth.AuthContext;
import de.fhdw.vendix.security.api.ui.ClassAccessChecker;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class DefaultClassAccessChecker implements ClassAccessChecker {

    @Override
    public boolean hasAccess(Class<?> viewClass) {
        Optional<AuthContext> ctx = AuthContextHolder.current();
        if (ctx.isEmpty() || ctx.get().accountRoles().isEmpty()) {
            return false;
        }

        SpringUserDetailsAdapter adapter = new SpringUserDetailsAdapter(ctx.get());
        Set<String> authorities = adapter.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        RolesAllowed rolesAllowed = viewClass.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) return true;

        return Arrays.stream(rolesAllowed.value()).anyMatch(authorities::contains);
    }
}
