package de.fhdw.vendix.commons.api.domain.account_role.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

public record AccountRoleDTO(
        long id,
        AccountRoleEnum role
) implements DomainDTO {
    public AccountRoleDTO {
        if (id < 0) {
            throw new IllegalArgumentException("RoleDTO parameter 'id' cannot be negative");
        }
        if (role == null) {
            throw new IllegalArgumentException("RoleDTO parameter 'role' cannot be null");
        }
    }
}