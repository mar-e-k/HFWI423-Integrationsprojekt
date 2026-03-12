package de.fhdw.vendix.store.app.app.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vendix.store.startup")
public record StartupProperties(
        Boolean cleanupLocks
) {
    public StartupProperties {
        cleanupLocks = cleanupLocks != null
                ? cleanupLocks
                : true;
    }
}