package de.fhdw.vendix.commons.spring.app.context.system;

import java.util.UUID;

public interface SystemContext {
    String getApplicationName();

    UUID getInstanceUuid();

    String getHostname();

    String getServerName();

    int getServerPort();
}