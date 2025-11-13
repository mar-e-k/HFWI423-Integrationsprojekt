package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contact_person")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContactPerson {

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
