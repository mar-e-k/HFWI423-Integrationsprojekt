package de.fhdw.vendix.commons.api.domain.account_role_assignment;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record AccountRoleAssignmentDTO(
    @Nullable Long id,
    AccountDTO account,
    AccountRoleDTO role
) implements DomainDTO<Long> {
    public AccountRoleAssignmentDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("AccountRoleAssignmentDTO parameter 'id' cannot be negative");
        }
        if (account == null) {
            throw new IllegalArgumentException("AccountRoleAssignmentDTO parameter 'account' cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("AccountRoleAssignmentDTO parameter 'role' cannot be null");
        }
    }

    @Override
    public @Nullable Long getIdentifiable() {
        return id;
    }
}