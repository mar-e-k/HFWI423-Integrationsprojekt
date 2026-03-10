package de.fhdw.vendix.commons.spring.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vendix.security")
public record SecurityPropertiesConfiguration(
        Boolean enabled
) {
    public SecurityPropertiesConfiguration {
        enabled = enabled != null
                ? enabled
                : true;
    }
}