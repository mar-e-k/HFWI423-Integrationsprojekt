package de.fhdw.vendix.commons.api.domain.connection;

import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;

public record ConnectionRequestDTO(
        EntityTargetDTO target,
        InstanceDetailsDTO instance
) implements RequestDTO {
    public ConnectionRequestDTO {
        if (target == null) {
            throw new IllegalArgumentException("ConnectionRequestDTO parameter 'target' cannot be null");
        }
        if (instance == null) {
            throw new IllegalArgumentException("ConnectionRequestDTO parameter 'instance' cannot be null");
        }
    }
}