package de.fhdw.vendix.orchestrator.core.domain.account_role_assignment;

import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.orchestrator.core.domain.account.Account;
import de.fhdw.vendix.orchestrator.core.domain.account_role.AccountRole;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class AccountRoleAssignment extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    private Account account;

    @ManyToOne(optional = false)
    private AccountRole role;

    protected AccountRoleAssignment() {}

    protected AccountRoleAssignment(Account account, AccountRole role) {
        this.account = account;
        this.role = role;
    }

    protected AccountRoleAssignment(Long id, Account account, AccountRole role) {
        super(id);
        this.account = account;
        this.role = role;
    }

    public Account getAccount() {
        return account;
    }

    public AccountRole getRole() {
        return role;
    }
}