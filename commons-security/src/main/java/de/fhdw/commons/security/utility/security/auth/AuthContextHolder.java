package de.fhdw.commons.security.utility.security.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class AuthContextHolder {

    private AuthContextHolder() {}

    public static Optional<AuthContext> current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthContext authContext) {
            return Optional.of(authContext);
        }
        return Optional.empty();
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}