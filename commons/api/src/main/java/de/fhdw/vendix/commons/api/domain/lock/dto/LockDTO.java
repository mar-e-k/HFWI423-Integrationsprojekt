package de.fhdw.vendix.commons.api.domain.lock.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.time.Instant;
import java.util.UUID;

public record LockDTO (
        long lockID,
        TargetTypeEnum targetType,
        long targetID,
        UUID instanceUUID,
        Instant acquiredAt,
        Instant expiresAt
) implements DomainDTO {
    public LockDTO {
        if (lockID < 0) {
            throw new IllegalArgumentException("LockDTO parameter 'lockID' cannot be negative");
        }
        if (targetType == null) {
            throw new IllegalArgumentException("LockDTO parameter 'targetType' cannot be null");
        }
        if (targetID < 0) {
            throw new IllegalArgumentException("LockDTO parameter 'targetID' cannot be negative");
        }
        if (instanceUUID == null) {
            throw new IllegalArgumentException("LockDTO parameter 'instanceUUID' cannot be null");
        }
        if (acquiredAt == null) {
            throw new IllegalArgumentException("LockDTO parameter 'acquiredAt' cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("LockDTO parameter 'expiresAt' cannot be null");
        }
        if (acquiredAt.isAfter(expiresAt)) {
            throw new IllegalArgumentException("LockDTO parameter 'acquiredAt' cannot be after parameter 'expiresAt'");
        }
    }
}