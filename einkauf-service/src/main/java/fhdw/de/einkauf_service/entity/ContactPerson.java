package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.*;

@Entity
@Table(name = "contact_person")
@Data
public class ContactPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String email;

    @ManyToMany(mappedBy = "contactPeople")
    private Set<Supplier> suppliers = new HashSet<>();
}
