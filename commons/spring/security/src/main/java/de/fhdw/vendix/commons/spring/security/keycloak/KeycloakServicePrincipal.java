package de.fhdw.vendix.commons.spring.security.keycloak;

public enum KeycloakServicePrincipal {
    ORCHESTRATOR_SERVICE("orchestrator-service"),
    SYSTEM_USER("system");

    private final String value;

    KeycloakServicePrincipal(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}