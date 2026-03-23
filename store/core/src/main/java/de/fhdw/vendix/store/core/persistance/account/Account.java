package de.fhdw.vendix.store.core.persistance.account;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.persistance.account_role_assignment.AccountRoleAssignment;
import de.fhdw.vendix.store.core.persistance.receipt.Receipt;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Entity
public class Account extends AbstractSpringDataAuditingEntity<Long> {

    @Column(unique = true, nullable = false)
    private UUID uuid;

//    Maybe add later

//    @Column(nullable = false)
//    @Pattern(regexp = "^\\+?(\\d{1,3})?[-.\\s]?(\\(?\\d{1,4}\\)?)[-.\\s]?(\\d{1,4})[-.\\s]?(\\d{1,9})$")
//    private String phone;
//
//    @Column(nullable = false)
//    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
//    private String email;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Account field 'accountUsername' cannot be blank")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Account field 'accountPassword' cannot be blank")
    private String password;

    @OneToMany(mappedBy = "account")
    private Set<AccountRoleAssignment> roles = new HashSet<>();

    @OneToMany(mappedBy = "cashier")
    private Set<Receipt> receipts = new HashSet<>();

    protected Account() {}

    protected Account(UUID uuid, String username, String password) {
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    @Default
    protected Account(@Nullable Long id, UUID uuid, String username, String password) {
        super(id);
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public UUID getUuid() {
        return uuid;
    }

//    public String getPhone() {
//        return phone;
//    }
//
//    public String getEmail() {
//        return email;
//    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<AccountRoleAssignment> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Set<Receipt> getReceipts() {
        return Collections.unmodifiableSet(receipts);
    }
}