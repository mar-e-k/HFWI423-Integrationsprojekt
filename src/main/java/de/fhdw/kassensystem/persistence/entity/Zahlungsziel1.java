package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "zahlungsziel")
public class Zahlungsziel1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zahlungsziel_id_gen")
    @SequenceGenerator(name = "zahlungsziel_id_gen", sequenceName = "zahlungsziel_zahlungsziel_id_seq", allocationSize = 1)
    @Column(name = "zahlungsziel_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "definition", nullable = false, length = 100)
    private String definition;

    @Column(name = "beschreibung", length = Integer.MAX_VALUE)
    private String beschreibung;

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

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

}