package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "payment_term")
public class PaymentTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_term_id_gen")
    @SequenceGenerator(name = "payment_term_id_gen", sequenceName = "payment_term_payment_term_id_seq", allocationSize = 1)
    @Column(name = "payment_term_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "definition", nullable = false, length = 100)
    private String definition;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

}