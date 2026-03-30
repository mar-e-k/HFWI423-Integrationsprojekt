package de.fhdw.vendix.store.core.persistance.account_role;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.persistance.account_role_assignment.AccountRoleAssignment;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Entity
public class AccountRole extends AbstractSpringDataAuditingEntity<Long> {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private de.fhdw.vendix.commons.api.domain.account_role.AccountRole role;

    @OneToMany(mappedBy = "role")
    private Set<AccountRoleAssignment> accounts = new HashSet<>();

    protected AccountRole() {}

    protected AccountRole(de.fhdw.vendix.commons.api.domain.account_role.AccountRole role) {
        this.role = role;
    }

    @Default
    protected AccountRole(@Nullable Long id, de.fhdw.vendix.commons.api.domain.account_role.AccountRole role) {
        super(id);
        this.role = role;
    }

    public de.fhdw.vendix.commons.api.domain.account_role.AccountRole getRole() {
        return role;
    }

    public Set<AccountRoleAssignment> getAccounts() {
        return Collections.unmodifiableSet(accounts);
    }
}