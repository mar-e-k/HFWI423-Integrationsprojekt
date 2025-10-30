package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.*;

@Entity
@Table(name = "supplier")
@Data
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier name is mandatory.")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Street is mandatory.")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "House number is mandatory.")
    @Column(nullable = false)
    private String houseNumber;

    @NotBlank(message = "ZIP code is mandatory.")
    @Column(nullable = false)
    private String zip;

    @NotBlank(message = "City is mandatory.")
    @Column(nullable = false)
    private String city;

    @Email(message = "Please enter a correct E-Mail.")
    @Size(max = 255, message = "E-Mail is too long.")
    private String email;

    @NotBlank(message = "Phone is mandatory.")
    @Column(nullable = false)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id", nullable = false)
    private PaymentTerm paymentTerm;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "connector_supplier_cp",
            joinColumns = @JoinColumn(name = "supplier_id"),
            inverseJoinColumns = @JoinColumn(name = "cp_id")
    )
    private Set<ContactPerson> contactPeople = new HashSet<>();
}
