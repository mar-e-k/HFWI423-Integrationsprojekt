package de.fhdw.vendix.commons.api.domain.connection;

import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record ConnectionDTO(
        @Nullable Long id,
        EntityTargetDTO target,
        InstanceDetailsDTO instance,
        Instant acquiredAt,
        Instant heartbeatAt
) implements DomainDTO<Long> {
    public ConnectionDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ConnectionDTO parameter 'id' cannot be negative");
        }
        if (target == null) {
            throw new IllegalArgumentException("ConnectionDTO parameter 'target' cannot be null");
        }
        if (instance == null) {
            throw new IllegalArgumentException("ConnectionDTO parameter 'instance' cannot be null");
        }
        if (acquiredAt == null) {
            throw new IllegalArgumentException("ConnectionDTO parameter 'acquiredAt' cannot be null");
        }
        if (heartbeatAt == null) {
            throw new IllegalArgumentException("ConnectionDTO parameter 'heartbeatAt' cannot be null");
        }
        if (heartbeatAt.isBefore(acquiredAt)) {
            throw new IllegalArgumentException("ConnectionDTO parameter 'heartbeatAt' cannot be before parameter 'acquiredAt'");
        }
    }

    @Override
    public @Nullable Long getIdentifiable() {
        return id;
    }
}