package de.fhdw.vendix.store.core.domain.account;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.account_role.AccountRole;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Account extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private AccountRole role;

    @OneToMany(mappedBy = "cashier")
    private List<Receipt> receipts = new ArrayList<>();

    @Column(unique = true, nullable = false)
    @NotNull(message = "Account field 'uuid' cannot be null")
    private UUID uuid;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Account field 'accountUsername' cannot be blank")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Account field 'accountPassword' cannot be blank")
    private String password;

    protected Account() {}

    protected Account(AccountRole role, List<Receipt> receipts, UUID uuid, String username, String password) {
        this.role = role;
        this.receipts = receipts;
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public AccountRole getRole() {
        return role;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}