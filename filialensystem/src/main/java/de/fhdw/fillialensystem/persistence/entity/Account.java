package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Account extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_role_id", nullable = false)
    private AccountRole accountRole;

    @Column(name = "account_id", unique = true, nullable = false)
    @NotNull(message = "Account Uuid cannot be null")
    private String uuid;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    public Account() {
        super();
    }

    public Account(AccountRole accountRole, String uuid, String username, String password) {
        this.accountRole = accountRole;
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String accountId) {
        this.uuid = accountId;
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
