package de.fhdw.vendix.commons.api.domain.distributed_lock;

import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record DistributedLockDTO(
        @Nullable Long id,
        EntityTargetDTO target,
        UUID instanceUuid,
        Instant acquiredAt,
        Instant expiresAt
) implements DomainDTO {
    public DistributedLockDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("DistributedLockDTO parameter 'id' cannot be negative");
        }
        if (target == null) {
            throw new IllegalArgumentException("DistributedLockDTO parameter 'target' cannot be null");
        }
        if (instanceUuid == null) {
            throw new IllegalArgumentException("DistributedLockDTO parameter 'instanceUuid' cannot be null");
        }
        if (acquiredAt == null) {
            throw new IllegalArgumentException("DistributedLockDTO parameter 'acquiredAt' cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("DistributedLockDTO parameter 'expiresAt' cannot be null");
        }
        if (acquiredAt.isAfter(expiresAt)) {
            throw new IllegalArgumentException("DistributedLockDTO parameter 'acquiredAt' cannot be after parameter 'expiresAt'");
        }
    }
}