package de.fhdw.vendix.store.application.initializer;

import de.fhdw.vendix.store.application.properties.StartupProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CleanupStoreHostsAndLocksInitializer  {

    private static final Logger log = LoggerFactory.getLogger(CleanupStoreHostsAndLocksInitializer.class);

    private final StartupProperties startupProperties;

    public CleanupStoreHostsAndLocksInitializer(StartupProperties startupProperties) {
        this.startupProperties = startupProperties;
    }

    @EventListener
    public void onApplicationStart(ApplicationReadyEvent event) {
        log.atInfo().log(startupProperties.toString());
        if (!startupProperties.cleanupLocks()) {
            return;
        }
        System.out.println("TODO");
    }
}