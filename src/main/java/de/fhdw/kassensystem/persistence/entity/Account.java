package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Account extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_role_id", nullable = false)
    @NotNull(message = "Account role cannot be null")
    private AccountRole accountRole;

    @Column(nullable = false, unique = true)
    @NotNull(message = "Account id cannot be null")
    private Integer accountId;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Password cannot be blank")
    private String password;

    public Account() {
        super();
    }

    public Account(AccountRole accountRole, Integer accountId, String username, String password) {
        this.accountRole = accountRole;
        this.accountId = accountId;
        this.username = username;
        this.password = password;
    }

    public Account(Long id, AccountRole accountRole, Integer accountId, String username, String password) {
        super(id);
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