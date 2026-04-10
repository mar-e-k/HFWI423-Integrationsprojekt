package de.fhdw.vendix.orchestrator.core.domain.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity
public class AccountRole extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role cannot be null")
    private Role role;

    protected AccountRole() {}

    protected AccountRole(Role role) {
        this.role = role;
    }

    @Default
    protected AccountRole(@Nullable Long id, Role role) {
        super(id);
        this.role = role;
    }

    public Role getRole() {
        return role;
    }
}