package de.fhdw.vendix.commons.api.domain.connection;

import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

import java.time.Instant;

public record ConnectionResponseDTO(
        Long id,
        EntityTargetDTO target,
        InstanceDetailsDTO instance,
        Instant acquiredAt
) implements ResponseDTO {
    public ConnectionResponseDTO {
        if (id < 0) {
            throw new IllegalArgumentException("ConnectionResponseDTO parameter 'id' cannot be negative");
        }
        if (target == null) {
            throw new IllegalArgumentException("ConnectionResponseDTO parameter 'target' cannot be null");
        }
        if (instance == null) {
            throw new IllegalArgumentException("ConnectionResponseDTO parameter 'instance' cannot be null");
        }
        if (acquiredAt == null) {
            throw new IllegalArgumentException("ConnectionResponseDTO parameter 'acquiredAt' cannot be null");
        }
    }
}