package de.fhdw.vendix.commons.api.domain.register.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

public record RegisterDTO(
        long registerId,
        long storeId
) implements DomainDTO {
    public RegisterDTO {
        if (registerId < 0) {
            throw new IllegalArgumentException("RegisterLineDTO parameter 'registerId' must be at least 0");
        }
        if (storeId < 0) {
            throw new IllegalArgumentException("RegisterLineDTO parameter 'storeId' must be at least 0");
        }
    }
}