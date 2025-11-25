package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "einheit")
public class Einheit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "einheit_id_gen")
    @SequenceGenerator(name = "einheit_id_gen", sequenceName = "einheit_einheit_id_seq", allocationSize = 1)
    @Column(name = "einheit_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 10)
    @NotNull
    @Column(name = "kuerzel", nullable = false, length = 10)
    private String kuerzel;

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

    public String getKuerzel() {
        return kuerzel;
    }

    public void setKuerzel(String kuerzel) {
        this.kuerzel = kuerzel;
    }

}