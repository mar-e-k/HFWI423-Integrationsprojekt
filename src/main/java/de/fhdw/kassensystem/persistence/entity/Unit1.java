package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "unit")
public class Unit1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unit_id_gen")
    @SequenceGenerator(name = "unit_id_gen", sequenceName = "unit_unit_id_seq", allocationSize = 1)
    @Column(name = "unit_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 10)
    @NotNull
    @Column(name = "abbreviation", nullable = false, length = 10)
    private String abbreviation;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

}