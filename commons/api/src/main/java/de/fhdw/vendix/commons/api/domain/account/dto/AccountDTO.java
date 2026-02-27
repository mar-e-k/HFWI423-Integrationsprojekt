package de.fhdw.vendix.commons.api.domain.account.dto;

import de.fhdw.vendix.commons.api.domain.role.dto.RoleDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

import java.util.Set;
import java.util.UUID;

public record AccountDTO(
        long id,
        UUID uuid,
        String username,
        String password,
        Set<RoleDTO> roles
) implements DomainDTO {
    public AccountDTO {
        if (id < 0) {
            throw new IllegalArgumentException("AccountDTO parameter 'id' cannot be negative");
        }
        if (uuid == null) {
            throw new IllegalArgumentException("AccountDTO parameter 'uuid' cannot be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("AccountDTO parameter 'username' cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("AccountDTO parameter 'password' cannot be null");
        }
        if (roles == null) {
            throw new IllegalArgumentException("AccountDTO parameter 'role' cannot be null");
        }
    }
}