package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.orchestrator.core.domain.account_role_assignment.AccountRoleAssignment;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Entity
public class Account extends AbstractSpringDataAuditingEntity<Long> {

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Account field 'accountUsername' cannot be blank")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Account field 'accountPassword' cannot be blank")
    private String password;

    @Column(nullable = false)
    @NotBlank(message = "Account field 'firstName' cannot be blank")
    private String firstName;

    @Nullable
    private String middleName;

    @Column(nullable = false)
    @NotBlank(message = "Account field 'lastName' cannot be blank")
    private String lastName;

    @Nullable
    private String phone;

    @Nullable
    @Email
    private String email;

    @OneToMany(mappedBy = "account")
    private Set<AccountRoleAssignment> roles = new HashSet<>();

    protected Account() {}

    protected Account(UUID uuid, String firstName, String lastName, String password) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    protected Account(UUID uuid, String username, String password, String firstName, String lastName) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
    }

    @Default
    protected Account(@Nullable Long id, UUID uuid, String username, String password, String firstName, @Nullable String middleName, String lastName, @Nullable String phone, @Nullable String email) {
        super(id);
        this.uuid = uuid;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.username = username;
        this.password = password;
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

    public String getFirstName() {
        return firstName;
    }

    public @Nullable String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public @Nullable String getPhone() {
        return phone;
    }

    public @Nullable String getEmail() {
        return email;
    }

    public Set<AccountRoleAssignment> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}