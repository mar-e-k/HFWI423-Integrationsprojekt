package de.fhdw.vendix.commons.spring.security.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vendix.keycloak.client")
public record KeycloakClientProperties (
    String appName,
    String webName
) {
    public KeycloakClientProperties {
        if (appName.isBlank()) {
            throw new IllegalStateException("vendix.keycloak.client.app-name is not set");
        }
        if (webName.isBlank()) {
            throw new IllegalStateException("vendix.keycloak.client.web-name is not set");
        }
        if (appName.equals(webName)) {
            throw new IllegalStateException(
                    "vendix.keycloak.client.app-name cannot equal vendix.keycloak.client.web-name"
            );
        }
    }
}