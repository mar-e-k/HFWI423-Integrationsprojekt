package de.fhdw.vendix.store.core.event.initializer;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

public class CleanupStoreHostsAndLocksInitializer {

    @EventListener
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        System.out.println("TODO");
    }
}