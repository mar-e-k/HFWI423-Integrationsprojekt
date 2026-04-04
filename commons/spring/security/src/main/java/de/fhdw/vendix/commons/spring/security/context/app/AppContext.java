package de.fhdw.vendix.commons.spring.security.context.app;

import java.util.UUID;

public interface AppContext {
    UUID getInstanceUUID();

    String getHostName();

    String getServerName();

    int getServerPort();
}