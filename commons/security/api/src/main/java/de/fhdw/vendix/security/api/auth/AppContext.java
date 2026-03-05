package de.fhdw.vendix.security.api.auth;

import java.util.UUID;

public interface AppContext {
    UUID getInstanceUUID();

    String getHostName();

    String getServerName();

    int getServerPort();
}