package de.fhdw.vendix.commons.api.domain.distributed_lock;

import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

import java.time.Instant;
import java.util.UUID;

public record DistributedLockRequestDTO(
        EntityTargetDTO entityTarget,
        UUID instanceUUID,
        Instant acquiredAt,
        Instant expiresAt
) implements RequestDTO {
    public DistributedLockRequestDTO {
        if (entityTarget == null) {
            throw new IllegalArgumentException("DistributedLockRequestDTO parameter 'target' cannot be null");
        }
        if (instanceUUID == null) {
            throw new IllegalArgumentException("DistributedLockRequestDTO parameter 'instanceUuid' cannot be null");
        }
    }
}