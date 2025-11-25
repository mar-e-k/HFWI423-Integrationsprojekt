package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Entity
@Table(name = "lieferant")
public class Lieferant {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lieferant_id_gen")
    @SequenceGenerator(name = "lieferant_id_gen", sequenceName = "lieferant_lieferant_id_seq", allocationSize = 1)
    @Column(name = "lieferant_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "strasse", nullable = false)
    private String strasse;

    @Size(max = 10)
    @NotNull
    @Column(name = "plz", nullable = false, length = 10)
    private String plz;

    @Size(max = 255)
    @NotNull
    @Column(name = "ort", nullable = false)
    private String ort;

    @Size(max = 100)
    @Column(name = "land", length = 100)
    private String land;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zahlungsziel_id")
    private Zahlungsziel zahlungsziel;

    @Size(max = 255)
    @Column(name = "email_bestellung")
    private String emailBestellung;

    @Size(max = 50)
    @Column(name = "telefon", length = 50)
    private String telefon;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "erstellt_am", nullable = false)
    private OffsetDateTime erstelltAm;

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

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public Zahlungsziel getZahlungsziel() {
        return zahlungsziel;
    }

    public void setZahlungsziel(Zahlungsziel zahlungsziel) {
        this.zahlungsziel = zahlungsziel;
    }

    public String getEmailBestellung() {
        return emailBestellung;
    }

    public void setEmailBestellung(String emailBestellung) {
        this.emailBestellung = emailBestellung;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public OffsetDateTime getErstelltAm() {
        return erstelltAm;
    }

    public void setErstelltAm(OffsetDateTime erstelltAm) {
        this.erstelltAm = erstelltAm;
    }

}