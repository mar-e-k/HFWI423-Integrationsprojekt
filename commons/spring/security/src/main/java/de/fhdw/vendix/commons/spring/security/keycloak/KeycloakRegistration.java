package de.fhdw.vendix.commons.spring.security.keycloak;

public enum KeycloakRegistration {
    ORCHESTRATOR("keycloak"),
    INVENTORY("inventory-client");

    private final String id;

    KeycloakRegistration(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}