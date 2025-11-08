package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AccountRole extends AbstractEntity {

    @OneToMany(mappedBy = "accountRole")
    private List<Account> accounts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @NotNull(message = "Role cannot be blank")
    private AccountRoleEnum role;

    public AccountRole() {
    }

    public AccountRole(List<Account> accounts, AccountRoleEnum role) {
        this.accounts = accounts;
        this.role = role;
    }

    public AccountRole(Long id, List<Account> accounts, AccountRoleEnum role) {
        super(id);
        this.accounts = accounts;
        this.role = role;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public AccountRoleEnum getRole() {
        return role;
    }

    public void setRole(AccountRoleEnum role) {
        this.role = role;
    }
}