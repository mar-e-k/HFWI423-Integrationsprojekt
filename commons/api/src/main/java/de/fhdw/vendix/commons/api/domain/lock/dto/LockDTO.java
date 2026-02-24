package de.fhdw.vendix.commons.api.domain.lock.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.time.Instant;
import java.util.UUID;

public record LockDTO (
        long lockId,
        Lock lockType,
        long targetId,
        UUID instanceId,
        Instant acquiredAt,
        Instant expiresAt
) implements DomainDTO {
    public LockDTO {
        if (lockId < 0) {
            throw new IllegalArgumentException("LockDTO parameter 'lockId' cannot be negative");
        }
        if (lockType == null) {
            throw new IllegalArgumentException("LockDTO parameter 'lockType' cannot be null");
        }
        if (targetId < 0) {
            throw new IllegalArgumentException("LockDTO parameter 'targetId' cannot be negative");
        }
        if (instanceId == null) {
            throw new IllegalArgumentException("LockDTO parameter 'instanceId' cannot be null");
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