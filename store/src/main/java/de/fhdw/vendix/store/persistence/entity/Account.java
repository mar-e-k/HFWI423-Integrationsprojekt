package de.fhdw.vendix.store.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Account extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private AccountRole accountRole;

    @OneToMany(mappedBy = "account")
    private List<Receipt> receipts = new ArrayList<>();

    @Column(unique = true, nullable = false)
    @NotNull(message = "Account uuid cannot be null")
    private String uuid;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Account username must not be blank")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Account password must not be blank")
    private String password;

    public Account() {
        super();
    }

    public Account(Long id) {
        super(id);
    }

    public Account(AccountRole accountRole, List<Receipt> receipts, String uuid, String username, String password) {
        this.accountRole = accountRole;
        this.receipts = receipts != null ? receipts : new ArrayList<>();
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public Account(Long id, AccountRole accountRole, List<Receipt> receipts, String uuid, String username, String password) {
        super(id);
        this.accountRole = accountRole;
        this.receipts = receipts;
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

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(getId(), account.getId()) && Objects.equals(accountRole, account.accountRole) && Objects.equals(uuid, account.uuid) && Objects.equals(username, account.username) && Objects.equals(password, account.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountRole, receipts, uuid, username, password);
    }
}