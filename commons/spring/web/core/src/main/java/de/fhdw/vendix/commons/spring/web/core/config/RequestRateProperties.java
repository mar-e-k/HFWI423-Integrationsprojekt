package de.fhdw.vendix.commons.spring.web.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "vendix.security.rate-limit")
public record RequestRateProperties(
        Boolean isEnabled,
        Long maxRequests,
        Duration resetTimeWindow
) {
    public RequestRateProperties {
        if (isEnabled == null) {
            isEnabled = true;
        }
        if (maxRequests == null) {
            maxRequests = 1000L;
        }
        if (maxRequests < 0) {
            throw new IllegalArgumentException("Attribute 'vendix.security.rate-limit.max-requests' must be a positive number");
        }
        if (resetTimeWindow == null) {
            resetTimeWindow = Duration.ofMinutes(1);
        }
    }
}