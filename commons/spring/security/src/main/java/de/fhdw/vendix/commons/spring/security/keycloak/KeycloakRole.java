package de.fhdw.vendix.commons.spring.security.keycloak;

import org.springframework.security.core.GrantedAuthority;

import java.util.Optional;

public enum KeycloakRole implements GrantedAuthority {

    CASHIER(Constants.CASHIER),
    ADMIN(Constants.ADMIN);

    private final String role;

    KeycloakRole(String role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return "ROLE_" + role;
    }

    public String getRole() {
        return role;
    }

    public static Optional<KeycloakRole> from(String value) {
        try {
            return Optional.of(KeycloakRole.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /**
     * IMPORTANT:
     * Annotation-safe constants (compile-time constants required by Java)
     * We use these so we can reference them from @RolesAllowed or similiar annotations
     */
    public static final class Constants {
        public static final String CASHIER = "CASHIER";
        public static final String ADMIN = "ADMIN";

        private Constants() {}
    }
}