package de.fhdw.vendix.commons.api.domain.account_role.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record AccountRoleDTO(
        @Nullable Long id,
        AccountRoleEnum role
) implements DomainDTO<Long> {
    public AccountRoleDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("RoleDTO parameter 'id' cannot be negative");
        }
        if (role == null) {
            throw new IllegalArgumentException("RoleDTO parameter 'role' cannot be null");
        }
    }

    @Override
    public @Nullable Long getIdentifiable() {
        return id;
    }
}