package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contact_person")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContactPerson {

    public ContactPerson(Long id, String firstName, String lastName, String role, String phone, String email, Set<Supplier> suppliers) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.suppliers = suppliers;
    }

    public ContactPerson() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Set<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "First name is mandatory.")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is mandatory.")
    @Column(nullable = false)
    private String lastName;

    @Size(max = 100)
    private String role;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()/]*$", message = "Please enter a valid phone number.")
    private String phone;

    @NotBlank(message = "E-Mail is mandatory.")
    @Email(message = "Please enter a valid E-Mail.")
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String email;

    @ManyToMany(mappedBy = "contactPeople", fetch = FetchType.LAZY)
    private Set<Supplier> suppliers = new HashSet<>();
}
