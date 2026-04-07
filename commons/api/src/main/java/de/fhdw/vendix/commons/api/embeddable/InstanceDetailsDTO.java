package de.fhdw.vendix.commons.api.embeddable;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

import java.util.UUID;

public record InstanceDetailsDTO(
        UUID uuid,
        String host,
        String server,
        Integer port
) implements EmbeddableDTO {
    public InstanceDetailsDTO {
        if (uuid == null) {
            throw new IllegalArgumentException("InstanceDetailsDTO parameter 'uuid' cannot be null");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("InstanceDetailsDTO parameter 'host' cannot be null or blank");
        }
        if (server == null || server.isBlank()) {
            throw new IllegalArgumentException("InstanceDetailsDTO parameter 'server' cannot be null or blank");
        }
        if (port == null || port < 0) {
            throw new IllegalArgumentException("InstanceDetailsDTO parameter 'port' cannot be null or negative");
        }
    }
}