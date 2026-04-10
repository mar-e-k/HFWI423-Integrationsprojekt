package de.fhdw.vendix.commons.api.domain.account_role_assignment;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record AccountRoleAssignmentDTO(
    @Nullable Long id,
    Long accountId,
    Long roleId
) implements DomainDTO {
    public AccountRoleAssignmentDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("AccountRoleAssignmentDTO parameter 'id' cannot be negative");
        }
        if (accountId == null || accountId < 0) {
            throw new IllegalArgumentException("AccountRoleAssignmentDTO parameter 'accountId' cannot be null");
        }
        if (roleId == null || roleId < 0) {
            throw new IllegalArgumentException("AccountRoleAssignmentDTO parameter 'roleId' cannot be null");
        }
    }
}