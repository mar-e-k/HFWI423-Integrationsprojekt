package de.fhdw.vendix.commons.spring.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "vendix.ui")
public record VaadinPropertiesConfiguration(
        Duration errorOverlayDuration,
        Boolean enableErrorOverlay
) {
    public VaadinPropertiesConfiguration {
        errorOverlayDuration = errorOverlayDuration != null
                ? errorOverlayDuration :
                Duration.ofSeconds(5);

        enableErrorOverlay = enableErrorOverlay != null
                ? enableErrorOverlay
                : true;
    }
}