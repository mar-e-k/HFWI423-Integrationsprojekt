package de.fhdw.vendix.commons.api.domain.system.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.util.UUID;

public record SystemDTO(
        UUID instanceId,
        String host,
        int port
) implements DomainDTO {
    public SystemDTO {
        if (instanceId == null) {
            throw new IllegalArgumentException("SystemDTO parameter 'instanceId' cannot be null");
        }
        if (host == null) {
            throw new IllegalArgumentException("SystemDTO parameter 'host' cannot be null");
        }
        if (host.isBlank()) {
            throw new IllegalArgumentException("SystemDTO parameter 'host' cannot be blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("SystemDTO parameter 'port' must be between 0 and 65535");
        }
    }
}