package de.fhdw.vendix.commons.spring.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vendix.orchestrator.client")
public record OrchestratorClientPropertiesConfiguration(
        String baseUrl
) {

    public OrchestratorClientPropertiesConfiguration {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
    }
}
