package de.fhdw.vendix.commons.api.domain.role.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

public record RoleDTO(
        long id,
        Role role
) implements DomainDTO {
    public RoleDTO {
        if (id < 0) {
            throw new IllegalArgumentException("RoleDTO parameter 'id' cannot be negative");
        }
        if (role == null) {
            throw new IllegalArgumentException("RoleDTO parameter 'role' cannot be null");
        }
    }
}