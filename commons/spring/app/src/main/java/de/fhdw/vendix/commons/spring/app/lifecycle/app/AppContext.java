package de.fhdw.vendix.commons.spring.app.lifecycle.app;

import java.util.UUID;

public interface AppContext {
    String getApplicationName();

    UUID getInstanceUUID();

    String getHostname();

    String getServerName();

    int getServerPort();
}