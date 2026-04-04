package de.fhdw.vendix.commons.api.embeddable;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

public record EntityTargetDTO(
        long id,
        TargetType type
) implements EmbeddableDTO {
    public EntityTargetDTO {
        if (id < 0) {
            throw new IllegalArgumentException("EntityTargetDTO parameter 'id' cannot be negative");
        }
        if (type == null) {
            throw new IllegalArgumentException("EmbeddableDTO parameter 'type' cannot be null");
        }
    }
}