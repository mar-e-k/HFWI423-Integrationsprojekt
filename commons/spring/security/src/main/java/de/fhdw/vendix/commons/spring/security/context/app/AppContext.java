package de.fhdw.vendix.commons.spring.security.context.app;

import java.util.UUID;

public interface AppContext {
    String getApplicationName();

    UUID getInstanceUUID();

    String getHostname();

    String getServerName();

    int getServerPort();
}