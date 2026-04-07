package de.fhdw.vendix.commons.api.domain.lock;

import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record LockDTO(
        @Nullable Long id,
        EntityTargetDTO target,
        UUID instanceUUID,
        Instant acquiredAt,
        Instant expiresAt
) implements DomainDTO {
    public LockDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("LockDTO parameter 'id' cannot be negative");
        }
        if (target == null) {
            throw new IllegalArgumentException("LockDTO parameter 'target' cannot be null");
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