package de.fhdw.vendix.commons.spring.app.lifecycle.shutdown;

import org.springframework.context.event.ContextClosedEvent;

public interface ShutdownHandler {
    void deleteLocksOnShutdown(ContextClosedEvent event);

    void deleteConnectionOnShutdown(ContextClosedEvent event);
}