package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Account extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_role_id", nullable = false)
    private AccountRole accountRole;

    @Column(name = "account_id", unique = true, nullable = false)
    @NotNull(message = "Account ID cannot be null")
    private Integer accountId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    public Account() {}

    public Account(AccountRole accountRole, Integer accountId, String username, String password) {
        this.accountRole = accountRole;
        this.accountId = accountId;
        this.username = username;
        this.password = password;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
