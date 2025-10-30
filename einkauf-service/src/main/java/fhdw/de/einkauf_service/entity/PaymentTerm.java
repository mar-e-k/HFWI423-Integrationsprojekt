package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payment_term")
@Data
public class PaymentTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String definition;

    private String description;
}
