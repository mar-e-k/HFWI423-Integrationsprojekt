package de.fhdw.vendix.store.core.domain.account_role;

import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.account.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AccountRole extends AbstractSpringDataAuditingEntity<Long> {

    @OneToMany(mappedBy = "role")
    private List<Account> accounts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @NotNull
    private AccountRoleEnum role;

    protected AccountRole() {}

    protected AccountRole(List<Account> accounts, AccountRoleEnum role) {
        this.accounts = accounts;
        this.role = role;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public AccountRoleEnum getRole() {
        return role;
    }
}