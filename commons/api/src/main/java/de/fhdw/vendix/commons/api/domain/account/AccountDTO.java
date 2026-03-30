package de.fhdw.vendix.commons.api.domain.account;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record AccountDTO(
        @Nullable Long id,
        UUID uuid,
        String username,
        String password
) implements DomainDTO<Long> {
    public AccountDTO {
        if (id != null && id < 0) {
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
    }

    @Override
    public @Nullable Long getIdentifiable() {
        return id;
    }
}