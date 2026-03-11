package de.fhdw.vendix.commons.api.domain.register.dto;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record RegisterDTO(
        @Nullable Long id,
        StoreDTO store
) implements DomainDTO {
    public RegisterDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("RegisterLineDTO parameter 'id' cannot be negative");
        }
        if (store == null) {
            throw new IllegalArgumentException("RegisterLineDTO parameter 'store' cannot be null");
        }
    }
}