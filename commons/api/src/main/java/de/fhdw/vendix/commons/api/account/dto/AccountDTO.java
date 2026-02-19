package de.fhdw.vendix.commons.api.account.dto;

import de.fhdw.vendix.commons.api.marker.dto.DomainDTO;
import de.fhdw.vendix.commons.api.role.Role;

import java.util.UUID;

public record AccountDTO(
        long id,
        UUID uuid,
        String username,
        String password,
        Role role
) implements DomainDTO {
    public AccountDTO {
        if (id < 1) {
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
        if (role == null) {
            throw new IllegalArgumentException("AccountDTO parameter 'role' cannot be null");
        }
    }
}