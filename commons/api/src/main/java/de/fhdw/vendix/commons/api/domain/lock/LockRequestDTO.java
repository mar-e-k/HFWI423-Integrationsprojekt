package de.fhdw.vendix.commons.api.domain.lock;

import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

import java.time.Instant;
import java.util.UUID;

public record LockRequestDTO(
        EntityTargetDTO entityTarget,
        UUID instanceUUID,
        Instant acquiredAt,
        Instant expiresAt
) implements RequestDTO {
    public LockRequestDTO {
        if (entityTarget == null) {
            throw new IllegalArgumentException("LockRequestDTO parameter 'target' cannot be null");
        }
        if (instanceUUID == null) {
            throw new IllegalArgumentException("LockRequestDTO parameter 'instanceUUID' cannot be null");
        }
    }
}