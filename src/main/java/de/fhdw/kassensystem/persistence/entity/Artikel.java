package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "artikel")
public class Artikel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artikel_id_gen")
    @SequenceGenerator(name = "artikel_id_gen", sequenceName = "artikel_artikelnummer_seq", allocationSize = 1)
    @Column(name = "artikelnummer", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "artikelname", nullable = false)
    private String artikelname;

    @NotNull
    @Column(name = "beschreibung", nullable = false, length = Integer.MAX_VALUE)
    private String beschreibung;

    @Size(max = 100)
    @NotNull
    @Column(name = "hersteller", nullable = false, length = 100)
    private String hersteller;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "lagerbestand", nullable = false)
    private Integer lagerbestand;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "einheit_id", nullable = false)
    private Einheit einheit;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lieferant_id", nullable = false)
    private Lieferant lieferant;

    @NotNull
    @Column(name = "einkaufspreis", nullable = false, precision = 10, scale = 2)
    private BigDecimal einkaufspreis;

    @NotNull
    @Column(name = "steuer_prozent", nullable = false, precision = 5, scale = 2)
    private BigDecimal steuerProzent;

    @Column(name = "verkaufspreis", precision = 10, scale = 2)
    private BigDecimal verkaufspreis;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getArtikelname() {
        return artikelname;
    }

    public void setArtikelname(String artikelname) {
        this.artikelname = artikelname;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getHersteller() {
        return hersteller;
    }

    public void setHersteller(String hersteller) {
        this.hersteller = hersteller;
    }

    public Integer getLagerbestand() {
        return lagerbestand;
    }

    public void setLagerbestand(Integer lagerbestand) {
        this.lagerbestand = lagerbestand;
    }

    public Einheit getEinheit() {
        return einheit;
    }

    public void setEinheit(Einheit einheit) {
        this.einheit = einheit;
    }

    public Lieferant getLieferant() {
        return lieferant;
    }

    public void setLieferant(Lieferant lieferant) {
        this.lieferant = lieferant;
    }

    public BigDecimal getEinkaufspreis() {
        return einkaufspreis;
    }

    public void setEinkaufspreis(BigDecimal einkaufspreis) {
        this.einkaufspreis = einkaufspreis;
    }

    public BigDecimal getSteuerProzent() {
        return steuerProzent;
    }

    public void setSteuerProzent(BigDecimal steuerProzent) {
        this.steuerProzent = steuerProzent;
    }

    public BigDecimal getVerkaufspreis() {
        return verkaufspreis;
    }

    public void setVerkaufspreis(BigDecimal verkaufspreis) {
        this.verkaufspreis = verkaufspreis;
    }

}