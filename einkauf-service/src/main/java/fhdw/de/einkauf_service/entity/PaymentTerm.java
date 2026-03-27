package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_term")
public class PaymentTerm {

    public PaymentTerm() {
    }

    public PaymentTerm(Long id, String definition, String description) {
        this.id = id;
        this.definition = definition;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String definition;

    private String description;
}
