package de.fhdw.vendix.store.app.properties;


import de.fhdw.vendix.store.commons.configuration.StartupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vendix.store.startup")
public record StartupConfiguration(
        boolean cleanupLocks
) implements StartupProperties {}