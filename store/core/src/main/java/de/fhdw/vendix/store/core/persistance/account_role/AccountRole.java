package de.fhdw.vendix.store.core.persistance.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.persistance.account_role_assignment.AccountRoleAssignment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.*;

@Entity
public class AccountRole extends AbstractSpringDataAuditingEntity<Long> {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @NotNull
    private AccountRoleEnum role;

    @OneToMany(mappedBy = "role")
    private Set<AccountRoleAssignment> accounts = new HashSet<>();

    protected AccountRole() {}

    protected AccountRole(AccountRoleEnum role) {
        this.role = role;
    }

    public AccountRoleEnum getRole() {
        return role;
    }

    public Set<AccountRoleAssignment> getAccounts() {
        return Collections.unmodifiableSet(accounts);
    }
}