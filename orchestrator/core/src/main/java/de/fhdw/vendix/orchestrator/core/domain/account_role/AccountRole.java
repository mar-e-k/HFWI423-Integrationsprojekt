package de.fhdw.vendix.orchestrator.core.domain.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.orchestrator.core.domain.account_role_assignment.AccountRoleAssignment;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Entity
public class AccountRole extends AbstractSpringDataAuditingEntity<Long> {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Role role;

    @OneToMany(mappedBy = "role")
    private Set<AccountRoleAssignment> accounts = new HashSet<>();

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

    public Set<AccountRoleAssignment> getAccounts() {
        return Collections.unmodifiableSet(accounts);
    }
}