package de.fhdw.vendix.commons.security.spring;

import de.fhdw.vendix.security.api.auth.AuthContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

// TODO: Potentially redundant
public final class AuthContextHolder {

    private AuthContextHolder() {}

    public static Optional<AuthContext> current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthContext authContext) {
            return Optional.of(authContext);
        } else {
            return Optional.empty();
        }
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}