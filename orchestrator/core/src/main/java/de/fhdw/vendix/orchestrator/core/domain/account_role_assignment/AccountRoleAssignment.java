package de.fhdw.vendix.orchestrator.core.domain.account_role_assignment;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "unique_account_role",
                columnNames = {"account_id", "role_id"}
        )
})
public class AccountRoleAssignment extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false)
    @Min(value = 1, message = "Account ID must be at least 1")
    @NotNull(message = "Account ID cannot be null")
    private Long accountId;

    @Column(nullable = false)
    @Min(value = 1, message = "Role ID must be at least 1")
    @NotNull(message = "Role ID cannot be null")
    private Long roleId;

    protected AccountRoleAssignment() {}

    protected AccountRoleAssignment(Long accountId, Long roleId) {
        this.accountId = accountId;
        this.roleId = roleId;
    }

    @Default
    protected AccountRoleAssignment(@Nullable Long id, Long accountId, Long roleId) {
        super(id);
        this.accountId = accountId;
        this.roleId = roleId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getRoleId() {
        return roleId;
    }
}