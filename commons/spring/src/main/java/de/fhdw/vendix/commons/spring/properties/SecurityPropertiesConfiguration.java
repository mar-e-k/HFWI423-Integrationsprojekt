package de.fhdw.vendix.commons.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "vendix.security")
@Validated
public record SecurityPropertiesConfiguration(
        @DefaultValue(value = "true")
        Boolean enabled
) {
    public SecurityPropertiesConfiguration {
        enabled = enabled != null
                ? enabled
                : true;
    }
}