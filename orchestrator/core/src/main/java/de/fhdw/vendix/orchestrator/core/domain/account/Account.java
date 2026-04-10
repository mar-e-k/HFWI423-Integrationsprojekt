package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Entity
public class Account extends AbstractSpringDataAuditingEntity<Long> {

    @Column(unique = true, nullable = false, updatable = false)
    @NotNull(message = "UUID cannot be null")
    private UUID uuid;

    @Column(unique = true, nullable = false)
    @NotNull(message = "Username cannot be null")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Column(nullable = false)
    @NotNull(message = "Password cannot be null")
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Nullable
    @Pattern(regexp = "^[\\p{L} '-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Nullable
    @Pattern(regexp = "^[\\p{L} '-]+$", message = "Middle name can only contain letters, spaces, hyphens, and apostrophes")
    private String middleName;

    @Nullable
    @Pattern(regexp = "^[\\p{L} '-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Column(unique = true)
    @Nullable
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number must be valid")
    private String phone;

    @Column(unique = true)
    @Email(message = "Email must be valid")
    @Nullable
    private String email;

    protected Account() {}

    public Account(UUID uuid, String username, String password) {
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public Account(@Nullable Long id, UUID uuid, String username, String password) {
        super(id);
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public Account(
            UUID uuid,
            String username,
            String password,
            @Nullable String firstName,
            @Nullable String middleName,
            @Nullable String lastName,
            @Nullable String phone,
            @Nullable String email
    ) {
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }


    @Default
    public Account(
            @Nullable Long id,
            UUID uuid,
            String username,
            String password,
            @Nullable String firstName,
            @Nullable String middleName,
            @Nullable String lastName,
            @Nullable String phone,
            @Nullable String email
    ) {
        super(id);
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
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

    public @Nullable String getFirstName() {
        return firstName;
    }

    public @Nullable String getMiddleName() {
        return middleName;
    }

    public @Nullable String getLastName() {
        return lastName;
    }

    public @Nullable String getPhone() {
        return phone;
    }

    public @Nullable String getEmail() {
        return email;
    }
}