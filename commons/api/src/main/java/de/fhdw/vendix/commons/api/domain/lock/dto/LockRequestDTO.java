package de.fhdw.vendix.commons.api.domain.lock.dto;

import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record LockRequestDTO(
        TargetTypeEnum targetType,
        long targetID,
        UUID instanceUUID,
        @Nullable Instant acquiredAt,
        @Nullable Instant expiresAt
) implements RequestDTO {
    public LockRequestDTO {
        if (targetType == null) {
            throw new IllegalArgumentException("LockRequestDTO parameter 'targetType' cannot be null");
        }
        if (targetID < 0) {
            throw new IllegalArgumentException("LockRequestDTO parameter 'targetId' cannot be negative");
        }
        if (instanceUUID == null) {
            throw new IllegalArgumentException("LockRequestDTO parameter 'instanceUUID' cannot be null");
        }
    }
}